## Bitonic Pipeline

This program creates a pipeline of sorting components,
implementing the parallel computing algorithm of bitonic sort.

We use java threads to handle the parallel computing, 
each thread only responsible assigned chunk of data. 

Once their chunck of data is finished computing, put their result
into the SynchronousQueue, for upper level of thread to use as input.

               0
             /   \
            1     2
           / \   / \
          3   4  5  6
          |   |  |  |
          7   8  9  10

In total we have 11 threads.
we use 7-10 as input creator
we use 3-5 as intial process
then merging upwards

