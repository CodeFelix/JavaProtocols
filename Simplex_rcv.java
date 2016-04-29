/*
 * Sistemas de Telecomunicacoes 
 *          2015/2016
 */
package protocol;

import simulator.Frame;
import terminal.NetworkLayer;

/**
 * Protocol 2 : Simplex Receiver protocol which does not transmit frames
 * 
 * @author jn.felix
 */
public class Simplex_rcv extends Base_Protocol implements Callbacks {

    public Simplex_rcv(Simulator _sim, NetworkLayer _net) {
        super(_sim, _net);      // Calls the constructor of Base_Protocol
        frame_expected = 0;
    }

    /**
     * CALLBACK FUNCTION: handle the beginning of the simulation event
     * @param time current simulation time
     */
    @Override
    public void start_simulation(long time) {
        sim.Log("\nSimplex Receiver Protocol\n\tOnly receive data!\n\n");
        //sim.Log("\nNot implemented yet\n\n");
    }

    /**
     * CALLBACK FUNCTION: handle the reception of a frame from the physical layer
     * @param time current simulation time
     * @param frame frame received
     */
    @Override
    public void from_physical_layer(long time, Frame frame) {
        sim.Log(time + " Frame received: " + frame.toString() + "\n");
        //sim.Log("from_physical_layer not implemented\n");
        
        if (frame.kind() == Frame.DATA_FRAME) {                                                     // Check the frame kind -> DATA 
            if (frame.seq() == frame_expected) {                                                    // Check the sequence number 
                net.to_network_layer(frame.info());                                                 // Send the frame to the network layer
                Frame ack_frame_received = Frame.new_Ack_Frame(frame_expected, null);               //  Cria um novo objecto do tipo ACK 
                sim.to_physical_layer(ack_frame_received);                                          //  Envia um ACK aquando a recepçao de tramas foi realizada
                frame_expected = next_seq(frame_expected);
            }else{                                                      
                
                Frame ack_frame_received = Frame.new_Ack_Frame(prev_seq(frame_expected), null);     //  Cria um novo objecto do tipo ACK, desta vez com o ACK anterior
                sim.to_physical_layer(ack_frame_received);                                          //  porque o frame do tipo de data foi recebido com uma ordem diferente
            }
            
        }
        
            
            
        
        
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
    private int frame_expected;
    
    /**
     * Reference to the simulator (Terminal), to get the configuration and send commands
     */
    //final Simulator sim;  -  Inherited from Base_Protocol
    
    /**
     * Reference to the network layer, to send a receive packets
     */
    //final NetworkLayer net;    -  Inherited from Base_Protocol
    
    /**
     * Expected sequence number of the next data frame received
     */
    
    
}
