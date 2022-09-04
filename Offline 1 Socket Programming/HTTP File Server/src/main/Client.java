package main;

import threads.ClientThread;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            new ClientThread(scanner.nextLine());
        }
    }
}

//localhost:8082
//localhost:8082/dir1/dir3/qstn.pdf
//localhost:8082/dir1/dir3/phy.pdf