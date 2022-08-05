## Symertic Bitonic Thread Implementation

The original concept of bitonic sort is to have each thread
responsible for their own chunk of data, and merge the data,
then have another thread in the higher level to handle the new data.

This implementation employed the cyclic barrier in java.concurrent library.
And the barriers are for thread to synchronize and collaborate to sort the data.
The barriers are stored in a heap structure with the amount of barriers as 1 << Grunulariy - 1
which is the level of the binary tree. 

                  no barrier at root
                /                    \
            barrier                 barrier
           /       \               /       \
        barrier    barrier       barrier    barrier

What we need to do is have each thread find the correct barrier it should wait and wait.
The determination of barrier is implemented in the awaitBarrier() method.

The original concept of iterative bitonic sort is to have k = 2 all the way to the len of the array.
And trickle down to compare distance of 1 which is by nature a bitonic sequence, and we iterate the value j = k / 2 to 1
Then finally, we start from the thread chunk start to the end, we comapre the other end of the compare distance j, which is i and i^j, 
we want to have up down development of the sequence
then we compare and swap the ones within the sweep, and once the index crossed the pivot, we need to look up down direction and swap.

This is really smart from my professor Kevin Lundeen.

Really appreciate your guidance.