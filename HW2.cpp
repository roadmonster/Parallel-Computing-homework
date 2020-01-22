/**
 * This is a program to calculate the prefixes of the given integer array.
 * The idea is to build a pairwise tree in parallel and then pass that tree into
 * a function calPrefixSum() to make use of the stored values and get the prefixes
 * of each element of the given array.
 *
 * @author Hao Li
 * CPSC5600
 * Seattle University 
 */

#include <iostream>
#include <vector>
#include <chrono>
#include <future>
#include <math.h>
#include <string>

using namespace std;
typedef vector<int> Data;
/**
 * Base class for HeapSum
 * This class is a array implemented heap. Each object of Heaper should consist
 * of the size of the data given, the ptr to the given data, the interior node ptr.
 * The interior nodes will store all the pairwise summations. 
 */
class Heaper{
public:
	Heaper(const Data* data):n(data->size()), data(data){
		interior = new Data(n-1, 0);
	}

	virtual ~Heaper(){
		delete interior;
	}
protected:
	
	int n; //size of the data given
	const Data* data; // ptr for the data
	Data* interior; // ptr for the interior node

	/**
	 * return the size of the all the elements of the heaper object: #of data given
	 *  plus the #of pairwise summation nodes 
 	 */
	virtual int size(){
		return (n - 1) + n;


	/**
 	 * @para i, the index of the node you want to get the value of summation 	
 	 * @return the value of pairwise summation at the index given
 	 * 
 	 */
	}
	virtual int value(int i){
		if(i < n - 1)
			return interior->at(i);
		else
			return data->at(i- (n-1));
	}

	/**
 	 * @para i the index which you want to get their parent's index
 	 * @return parent's index
 	 */
	virtual int parentID(int i){
        return (i - 1) / 2;
	}

	/**
 	 * @para i the index
 	 * @return left child index
 	 */
	virtual int leftChildID(int i){
		return i*2 + 1;
	}

	/**
	 * get the right child index
 	 */
	virtual int rightChildID(int i){
		return i*2 + 2;
	}

	/**
 	 * Tell if current node is a leaf
 	 */
	virtual bool isLeaf(int i){
		if( i >= n - 1){
			return true;
		}
		else
			return false;
	}
	
};

/**
 * SumHeap class a children class of Heaper
 * This class does three things: 
 *
 */

class SumHeap: public Heaper{
public:
  SumHeap(const Data* data) : Heaper(data){
	   calcSum(0);
  }
    
  int sum(int node = 0){
	   return value(node);
   }

  const int ROOT_PREFIX = 0;
   /**
 	 * This method first build up the prefix tree then write the prefix of the leaf
 	 * node into the output array.
 	 */
  void prefixSums(Data* prefix){
     calPrefixSum(prefix, 0, ROOT_PREFIX, 0);
   }
    
private:
	static const int PARAL_BOUNDRY = 4; // only allowed to fork the first 4 levels

	/**
 	 * This method returns the ID of the last node that could be paralleled
 	 * @return the nodeID
 	 */
	int get_last_para_node(){
		int last_para_nodeID = 0;

		for(int j = 1; j <= PARAL_BOUNDRY - 1; j++){
			last_para_nodeID += (2^j);
		}
		return last_para_nodeID;
	}

	/**
 	 * This method recursivly calculates the pairwise summation
 	 * @para node id
 	 * 
 	 */
	int calcSum(int i){
		
		//get the last parallel possible id
		int last_para_nodeID = get_last_para_node();
		if(isLeaf(i))
			return value(i);
		
		//use main thread to calculate the left subtree
		calcSum(leftChildID(i));

		//if the rightChild node id is less the last possible id# then fork and parallel
		if( rightChildID(i) <= last_para_nodeID ){
			auto handle = async(launch::async, &SumHeap::calcSum, this, rightChildID(i));
		
			interior->at(i) = handle.get() + value(leftChildID(i));
		}
		// otherwise, use the main thread to calculate the right subtree.
		else{
			calcSum(rightChildID(i));
			interior->at(i) = value(leftChildID(i)) + value(rightChildID(i));
			
		}

		return interior->at(i);
		
	}
	
	/**
 	 * This is a recursive method to calculate the prefixes

 	 * The idea is to distinguish the input into left or right  subtree request.
 	 * And calculate them in parallel.
 	 * The interior node's prefix will be exclusive, 
 	 * The leaf nodes' prefix will be inclusive.
 	 
 	 * @para Data*, the ptr for the output array
 	 * @para index, the index showing the node ID
 	 * @para my_parent_prefix, the prefix of my parent node
 	 * @para flag the flag indicating (0: calculate left subtree, 1: calculate right subtree)  
 	 */
  
  void calPrefixSum(Data* output, int index, int my_parent_prefix, int flag){
      int my_leftChild = leftChildID(index);
      int my_rightChild = rightChildID(index);
      int my_prefix = 0;
      int last_para_nodeID = get_last_para_node();
      if(!flag){
      	my_prefix = my_parent_prefix;

      }
      else{
      	my_prefix = my_parent_prefix + value(index - 1);
      }

      if(isLeaf(index) && (index < output->size())){
      	output->at(index - (n-1)) = my_prefix + value(index);
			return;	
		}
      calPrefixSum(output, my_leftChild, my_prefix, 0);

      if(my_rightChild <last_para_nodeID){
      	auto handle = async(launch::async, &SumHeap::calPrefixSum, this, output, my_rightChild, my_prefix, 1);
      }
      else
      	calPrefixSum(output, my_rightChild, my_prefix, 1);
  }


  


		
};

const int N = 1<<26;  // must be power of 2 for now

int main() {
    Data data(N, 1);  // put a 1 in each element of the data array
    Data prefix(N, 1);

    // start timer
    auto start = chrono::steady_clock::now();

    SumHeap heap(&data);
    heap.prefixSums(&prefix);

    // stop timer
    auto end = chrono::steady_clock::now();
    auto elpased = chrono::duration<double,milli>(end-start).count();

    int check = 1;
    for (int elem: prefix)
        if (elem != check++) {
            cout << "FAILED RESULT at " << check-1;
            break;
        }
    cout << "in " << elpased << "ms" << endl;
    return 0;
}


