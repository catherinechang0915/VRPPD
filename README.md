# VRPPD

In this project, traditional VRPPDTW problems were extended to take priority constraints into consideration. It is useful in real life to model the cases when customers have various levels of service quality requirement. A model for real-life food delivery was proposed using linear integer programming model. In our model, customers are categorized as members and non-members, and a shortest route was desired solution when satisfying members' time requirements. An investigation of the influence of member percentage in the data and model parameters were conducted, results were presented in the thesis.

This repository implements the ALNS algorithm proposed by the following paper to solve our model

```
@article{
  title={An adaptive large neighborhood search heuristic for the pickup and delivery problem with time windows},
  author={Ropke, Stefan and Pisinger, David},
  journal={Transportation science},
  volume={40},
  number={4},
  pages={455--472},
  year={2006},
  publisher={Informs}
}
```

Model based on the VRPPDTW by adding membership status for each request was proposed.
The objective was set to minimize the weighted sum of distance and delay.
The algorithm was modified as follows
1. Feasibility check only checked time window constraints for members' requests
2. Acceptance criterion was change to Threshold Acceptance
3. The difference of membership status was added into the similarity definition in Shaw Removal

## data
The data set in `data` was generated from Li and Lim instances in `raw_data` by
1. shrink time window by percentage value available in `src/Utils`
2. assign $`m_p`$ percentage of the requests with membership status

The data folder is named as $`pdp_<size>_mem_<m_p>`$

## src
`DataStructures` stores the representation structure of a solution; input and output parameters

`Examples` contains data for one toy example and model file for CPLEX OPL

`Operators` implements removal and insert heuristics

`ALNSSolver` implements the ALNS framework

`Main` runs the program from command line with
```math
2 <size> <m_p> <\alpha> <\beta> <noise>
```
