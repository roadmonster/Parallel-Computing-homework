#pragma once
#include <pthread.h>

template<typename P>
class ThreadGroup{
private:
    struct Node{
        Node *next;
        pthread_t thread;
        int id;
        void *sharedData;

        Node(int id, void *sharedData, Node *next)
            : next(next), thread(0), id(id), sharedData(sharedData){

            }
    };

    Node *head;

    static void* startThread(void* args){
        Node *node = (Node*)args;
        P process;
        process(node->id, node->sharedData)
        return nullptr;
    }

public:
    ThreadGroup(): head(nullptr){
    }

    void createThread(int id, void* sharedData){
        head = new Node(id, sharedData, head);
        pthread_create(&(head->thread), nullptr, &ThreadGroup<P>::startThread, head);
    }

    void waitForAll(){
        while(head != nullptr){
            pthread_join(head->thread, nullptr);
            Node* done = head;
            head = head->next;
            delete done;
        }
    }

};