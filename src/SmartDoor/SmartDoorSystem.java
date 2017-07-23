package SmartDoor;

/**
 * Implementation of the HomeSecuritySystem. 
 * 
 * @author khaves
 */
//<editor-fold defaultstate="collapsed" desc="comment">
    /*
    Server:
    ClientArray
    Framework Project URL
    */
//</editor-fold>
public class SmartDoorSystem {
    private final short SIZE = 5;
    int ActiveClients=0;
    Client[] ClientArray = new Client[SIZE];
    
    public SmartDoorSystem() {
        for (int i = 0; i < SIZE; i++) {
            this.ClientArray[i] = new Client (0, "<empty>");
        }
    };

//<editor-fold defaultstate="collapsed" desc="comment">
    /*
    Client:
    -ID
    -IP
    -Framework Thing URL
    -Beschreibung
    */
//</editor-fold>
    private class Client {
        int id;
        String ip;
        Thread ThreadHandle;

        public Client(int i, String ip) {
            id = i;
            this.ip = ip;
            this.ThreadHandle = null;
        }
         
        public void reset () {
            id = 0;
            ip = "<empty>";
            ThreadHandle = null;
        }
    }
    
    /**
     * Add a Client.
     * Adds a Client to the System. 
     * 
     * @param ip IP of Camera 
     * @return TRUE if camera was addes to the HomeSecSystem.
               FALSE if no more cameras vould be added to the HomeSecSystem.
     */
    public int createEntry (String ip) {
        for (int i = 0; i < SIZE; i++) {
            if (ClientArray[i].ip.equals("<empty>")){
               ClientArray[i].id=i+1;
               ClientArray[i].ip=ip;
               ActiveClients++;
               return ClientArray[i].id;
            }
        }
        return 0;    
    };
    
    /**
     * Delete Entry by id.
     * 
     * @param idx Id of Camera that should be deleted
     * @return true Entry could be deleted/false Entry doesnt exist
     */
    public boolean deleteEntry (int idx) {
        if(ClientArray[idx-1].id!=0 && idx < 5 && idx > 0){
            ClientArray[idx-1].reset();
            ActiveClients--;
            return true;
        }
        else
            return false;
    }
}

