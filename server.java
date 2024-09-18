/* Usage ---------------------------
 *
 * Run "rmiregistry 1099" from a terminal window
 *
 * Run "java server" in a different terminal 
 *
 * Run "java client" in a third terminal to simulate the events
 *
 */


import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

//interface to be used by the VS implementation
interface VectorClock extends java.rmi.Remote {
    int[] getClock() throws RemoteException;
    void increment(int processId) throws RemoteException;
    void update(int[] remoteClock) throws RemoteException;
}

//VC implementation using VC interface
class VectorClockImpl extends UnicastRemoteObject implements VectorClock {
    private int[] clock;

    //initializing vector clock and setting all values to 0
    protected VectorClockImpl(int numProcesses) throws RemoteException {
        clock = new int[numProcesses];  
        Arrays.fill(clock, 0);  
    }

    //getter method for the clock 
    public synchronized int[] getClock() throws RemoteException {
        return clock;
    }

    //function to increment passed clock
    public synchronized void increment(int processId) throws RemoteException {
        clock[processId]++;  
    }

    //updating clock by taking maxs of each VC 
    public synchronized void update(int[] remoteClock) throws RemoteException {
        for (int i = 0; i < clock.length; i++) {
            clock[i] = Math.max(clock[i], remoteClock[i]); 
        }
    }
}


//remote process interface definign functions to be used in implementation
interface RemoteProcess extends java.rmi.Remote {
    VectorClock getVectorClock() throws RemoteException;
    void sendEvent(int eventId, String data, int targetProcessId) throws RemoteException;
}


class RemoteProcessImpl extends UnicastRemoteObject implements RemoteProcess {
    //variable definition 
    private final int processId;
    private final VectorClock vectorClock;

    //contructor for process ID's and individual clocks
    protected RemoteProcessImpl(int processId, int numProcesses) throws RemoteException {
        this.processId = processId;
        this.vectorClock = new VectorClockImpl(numProcesses);
    }

    //getter method for the vactor clock of process that called it
    public VectorClock getVectorClock() throws RemoteException {
        return vectorClock;
    }

    public synchronized void sendEvent(int eventId, String data, int targetProcessId) throws RemoteException {
        try {
            //incrementing vactor clock of sender process
            vectorClock.increment(processId);
            System.out.println("Process " + processId + " sends event " + eventId + " with data: " + data);

            //getting reference of receiver process from rmi registry
            RemoteProcess targetProcess = (RemoteProcess) Naming.lookup("rmi://localhost/Process" + targetProcessId);

            //getting VC of receiver process
            int[] targetClock = targetProcess.getVectorClock().getClock();

            //printing vector clocks
            System.out.println("Process " + processId + " = " + Arrays.toString(vectorClock.getClock()));
            vectorClock.update(targetClock);
            System.out.println("Process " + processId + " = " + Arrays.toString(vectorClock.getClock()));

        } catch (java.rmi.NotBoundException e) {
            System.err.println("Error locating process");
        } catch (java.net.MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
        }
    }
}

public class server {
    public static void main(String[] args) {
        try {
	    //simulating 3 processes
            int numProcesses = 3;  

            //creating the 3 processes and binding to rmi registry
            for (int i = 0; i < numProcesses; i++) {
                RemoteProcessImpl process = new RemoteProcessImpl(i, numProcesses);
                Naming.rebind("rmi://localhost/Process" + i, process);
                System.out.println("Process " + i + " is ready.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

