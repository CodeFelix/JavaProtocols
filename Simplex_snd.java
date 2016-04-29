/*
 * Sistemas de Telecomunicacoes 
 *          2015/2016
 */
package protocol;

import simulator.Frame;
import terminal.NetworkLayer;

/**
 * Protocol 2 : Simplex Sender protocol which does not receive frames
 * 
 * @author jn.felix
 */
public class Simplex_snd extends Base_Protocol implements Callbacks {

    public Simplex_snd(Simulator _sim, NetworkLayer _net) {
        super(_sim, _net);      // Calls the constructor of Base_Protocol
        next_frame_to_send = 0;
    }

    /**
     * CALLBACK FUNCTION: handle the beginning of the simulation event
     * @param time current simulation time
     */
    @Override
    public void start_simulation(long time) {
        sim.Log("\nSimplex Sender Protocol\n\tOnly send data!\n\n");
        send_next_data_packet();                                 //  Starting send first packet
        //sim.Log("\nNot implemented yet\n\n");
    }

    /**
     * CALLBACK FUNCTION: handle the end of Data frame transmission, start timer
     * @param time current simulation time
     * @param seq  sequence number of the Data frame transmitted
     */
    @Override
    public void handle_Data_end(long time, int seq) {
        
        //sim.Log("handle_Data_end not implemented\n");
    }
    
    /**
     * CALLBACK FUNCTION: handle the timer event; retransmit failed frames
     * @param time current simulation time
     * @param key  timer key
     */
    @Override
    public void handle_Data_Timer(long time, int key) {
        sim.Log(time + " Data Timeout ("+key+")\n");                       
        retransmission_data_packet();
        //sim.Log("handle_Data_Timer not implemented\n");
    }
    
    /**
     * CALLBACK FUNCTION: handle the reception of a frame from the physical layer
     * @param time current simulation time
     * @param frame frame received
     */
    @Override
    public void from_physical_layer(long time, Frame frame) {
        sim.Log(time + " Frame received: " + frame.toString() + "\n");
        
        if(frame.kind() == Frame.ACK_FRAME){                            //  Se a trama é do tipo ACK
            if(frame.ack() == prev_seq(next_frame_to_send)){
                //  Se receber ACK que confirma que o frame foi recebido, envia proximo packet 
                send_next_data_packet();

            }
        }
        
            
        
        //sim.Log("from_physical_layer not implemented\n");
    }
    
    /**
     * Fetches the network layer for the next packet and starts it transmission
     * @return true is started data frame transmission, false otherwise
     */   
    public boolean send_next_data_packet(){
        //   We can only send one Data packet at a time
        //   you must wait for the DATA_END event before transmitting another one
        //   otherwise the first packet is lost in the channel
        String packet = net.from_network_layer();
        
        last_packet = packet;                       //  String Packet para retransmissão se houver uma falha
        
        if(packet != null){
            // The ACK field of the DATA frame is always the sequence number before zero, because no packets will be received
            Frame frame = Frame.new_Data_Frame(next_frame_to_send, prev_seq(0), null, packet);

            sim.to_physical_layer(frame); 
            sim.start_data_timer(next_frame_to_send);
            
            next_frame_to_send = next_seq(next_frame_to_send);            //   Incrementa o next_frame_to_send
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @return true is started frame retrasnmission, false  otherwise
     */
    public boolean retransmission_data_packet(){
        
            if(last_packet != null){
                Frame frame_retransmitted = Frame.new_Data_Frame(prev_seq(next_frame_to_send), prev_seq(0), null, last_packet);     //  Cria uma nova trama com a informação do pacote que falhou o envio
                sim.to_physical_layer(frame_retransmitted);
                
                sim.start_data_timer(prev_seq(next_frame_to_send));                                                            
                return true;
            }
            return false;
    }
    

    /**
     * CALLBACK FUNCTION: handle the end of the simulation
     * @param time current simulation time
     */
    @Override
    public void end_simulation(long time) {
        sim.Log("Stopping simulation\n");
    }
    
    
    /* Variables */
    private int next_frame_to_send;   
   
    
    private String last_packet;
    
    /**
     * Reference to the simulator (Terminal), to get the configuration and send commands
     */
    //final Simulator sim;  -  Inherited from Base_Protocol
    
    /**
     * Reference to the network layer, to send a receive packets
     */
    //final NetworkLayer net;    -  Inherited from Base_Protocol
    
    /**
     * Sequence number of the next data frame
     */


}
