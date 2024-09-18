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

public class client {
    public static void main(String[] args) {
        try {
            //getting reference to each process created from server 
            RemoteProcess process0 = (RemoteProcess) Naming.lookup("rmi://localhost/Process0");
            RemoteProcess process1 = (RemoteProcess) Naming.lookup("rmi://localhost/Process1");
            RemoteProcess process2 = (RemoteProcess) Naming.lookup("rmi://localhost/Process2");

            //simulating events between processes for clocks to update
            process0.sendEvent(1, "Message from Process 0 to Process 1", 1);  //P0 to P1
            process1.sendEvent(2, "Message from Process 1 to Process 2", 2);  //P1 to P2
            process2.sendEvent(3, "Message from Process 2 to Process 0", 0);  //P2 to P0

	  //printing any exceptions that are thrown
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

