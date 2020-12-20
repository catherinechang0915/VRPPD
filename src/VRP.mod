/*********************************************
 * OPL 12.8.0.0 Model
 * Author: 7
 * Creation Date: 2020.10.26
 *********************************************/
int n = ...;
range pickup = 1..n;
range delivery = (n+1)..(2*n);
int startDepot = 0;
int endDepot = 2*n+1;
range node = startDepot..endDepot;
range nodeN = 1..(2*n);

int K = ...;
range vehicle = 1..K;

tuple nodes {
	int q; // > 0 for pickup, < 0 for delivery
	int tw1;
	int tw2;
	int s; // service time
}

nodes nodeInfo[node] = ...;

float X[node] = ...;
float Y[node] = ...;

tuple edges {
	int node1;
	int node2;
}

//{edges} edge = {<i,j>|i in node, j in node: i != j && i != 0 && j != 2*n+1};
{edges} edge = {};
execute initializeEdge{
	for (var i in node) {
    	for (var j in node) {
    		if (i != j) {
      			if (i == 0) {
      		  		if (j >= 1 && j <= n) edge.add(i,j);
      			} else if (j == 2*n+1) {
      		  		if (i > n && i <= 2*n) edge.add(i,j);
      			} else {
      		  		edge.add(i,j);
      			}
    		}
		}		
  	}
  	edge.add(0,2*n+1);
}


float d[edge]; // distance
float t[edge] = d; // travel time

int membership[node] = ...;
{int} member = {i|i in node: membership[i] == 1};
{int} nonmember = {i|i in node: membership[i] == 0};

int capacity[vehicle] = ...;

// decision variables
dvar boolean x[edge][vehicle];
dvar float+ Q[node][vehicle];
dvar float+ T[node][vehicle];
dvar float+ DL[node][vehicle];

execute INITIALIZE{
	for (var e in edge){
		d[e] = Math.sqrt(Math.pow(X[e.node1]-X[e.node2], 2) + Math.pow(Y[e.node1]-Y[e.node2], 2));
	}
	// ensure the load and service time at depot is 0
	nodeInfo[0].q = 0;
	nodeInfo[0].s = 0;
	nodeInfo[2*n+1].q = 0;
	nodeInfo[2*n+1].s = 0;
}

execute {
cplex.tilim = 1800;
}

// scale factor
float alpha = 3;
float beta = 1;

// model
minimize

alpha*sum(k in vehicle, e in edge)(d[e]*x[e][k])
	+ beta*sum(k in vehicle, i in node)DL[i][k];

subject to {

   // each customer should be visited for at most 1 time
   forall(i in pickup)
     sum(k in vehicle, e in edge: e.node1 == i && e.node2 in nodeN)x[e][k] == 1;
     
   // flow conservation at nodes except depots
   forall (i in nodeN, k in vehicle)
     sum(e in edge: e.node1 == i)x[e][k] == sum(e in edge: e.node2 == i)x[e][k];
     
   // start depot
   forall (k in vehicle)
     sum(e in edge: e.node1 == 0 && (e.node2 in pickup || e.node2 == (2*n+1)))
       x[e][k] == 1;
//	sum(e in edge: e.node1 == 0)
//       x[e][k] == 1;
       
   // end depot
   forall(k in vehicle)
     sum(e in edge: e.node2 == (2*n+1) && (e.node1 in delivery || e.node1 == 0))
       x[e][k] == 1;
//	 sum(e in edge: e.node2 == (2*n+1))
//       x[e][k] == 1;
     
   // same vehicle for pickup and delivery
   forall(i in pickup, k in vehicle)
     sum(e in edge: e.node1 == i && e.node2 in nodeN)x[e][k] == 
     sum(e in edge: e.node1 in nodeN && e.node2 == (n+i))x[e][k];
     
   // load change
   forall(e in edge, k in vehicle)
     Q[e.node1][k] + nodeInfo[e.node2].q - Q[e.node2][k] <= 10000*(1 - x[e][k]);
     
   // initial load value
   forall(k in vehicle)
     Q[0][k] == 0;
     
   // capacity constraints
   forall(i in pickup, k in vehicle) {
     nodeInfo[i].q <= Q[i][k];
     Q[i][k] <= capacity[k];
   }
   forall(i in delivery, k in vehicle) {
//     0 <= Q[i][k]; // variable is nonnegative
     Q[i][k] <= capacity[k] + nodeInfo[i].q;
   }
   
   // time change
   forall(e in edge, k in vehicle) {
     T[e.node1][k] + nodeInfo[e.node1].s + t[e] - T[e.node2][k] 
     <= 10000*(1-x[e][k]);
   }     
     
   // pickup before delivery
   forall(e in edge: e.node1 in pickup && e.node2 == e.node1 + n, k in vehicle) {
     T[e.node1][k] + nodeInfo[e.node1].s + t[e] <= T[e.node2][k];
   }     
      
   // hard TW for members
   forall(i in member, k in vehicle) {
     nodeInfo[i].tw1 <= T[i][k];
     T[i][k] <= nodeInfo[i].tw2;
   }
//   
   // soft TW for others
   forall(i in nonmember, k in vehicle) {
     nodeInfo[i].tw1 <= T[i][k];
     T[i][k] - nodeInfo[i].tw2 - DL[i][k] 
     <= 10000*(1-sum(e in edge: (e.node1 in nodeN || e.node1 == 0) && e.node2 == i)x[e][k]);
   }
}

execute outputresult{
   var ofile = new IloOplOutputFile("Solution.txt",false);
   var o1 = 0, o2 = 0, o3 = 0;
   for (var k in vehicle){
       ofile.writeln("vehicle " + k + ":");
       for (var e in edge){
           if(x[e][k] == 1){
              ofile.writeln("travel from "+ e.node1 +" to "+e.node2 + " travels " + d[e]);
              o1 += d[e];
           }
       }
       for (var i in node) {
         o3 += DL[i][k];
       }
       o2 += T[endDepot][k] - T[startDepot][k];
   }
   ofile.writeln("objective part 1 " + o1 + " " + alpha*o1);
   ofile.writeln("objective part 2 " + o3 + " " + beta*o3);
//   ofile.writeln("objective part 3 " + o3 + " " + gamma*o3);
}