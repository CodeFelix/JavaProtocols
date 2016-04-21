/*
 * Sistemas de Telecomunicacoes 
 *          2015/2016
 */
package protocol;

import simulator.Frame;
import terminal.NetworkLayer;
import terminal.Terminal;

/**
 * Protocol 3 : Stop & Wait protocol
 * 
 * @author lflb@fct.unl.pt
 */
public class StopWait extends Base_Protocol implements Callbacks {

    public StopWait(Simulator _sim, NetworkLayer _net) {
        super(_sim, _net);      // Calls the constructor of Base_Protocol
        // Initialize here all variables
        next_frame_to_send = 0;
        frame_expected = 0; 
    }



    /**
     * CALLBACK FUNCTION: handle the beginning of the simulation event
     * @param time current simulation time
     */
    @Override
    public void start_simulation(long time) {
        sim.Log("\nStop&Wait Protocol\n\n");
        send_next_data_packet();

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
        sim.start_data_timer(seq);          //  Start timer

    }
    
    /**
     * CALLBACK FUNCTION: handle the timer event; retransmit failed frames
     * @param time current simulation time
     * @param key  timer key
     */
    @Override
    public void handle_Data_Timer(long time, int key) {
        sim.Log(time + " Data Timeout ("+key+")\n");
        retransmission_data_packet();                      //   Retrasmit failed frames
        
        
        //sim.Log("handle_Data_Timer not implemented\n");
    }
    
    /**
     * CALLBACK FUNCTION: handle the ack timer event; send ACK frame
     * @param time current simulation time
     */
    @Override
    public void handle_ack_Timer(long time) {
        if (Terminal.debug) {
            sim.Log(time + "Ack_timeout\n");
        }
        sim.cancel_ack_timer();
        Frame ack_frame = Frame.new_Ack_Frame(frame_expected, null);            //  Cria um novo objecto do tipo ACK
        sim.to_physical_layer(ack_frame);                                       //  Send ACK frame -> Envia uma Trama do tipo ACK para a camada física quando a recceção de tramas é realizada 
        //sim.Log("handle_ack_Timer not implemented\n");
        
    }
    
    /**
     * CALLBACK FUNCTION: handle the reception of a frame from the physical layer
     * @param time current simulation time
     * @param frame frame received
     */
    @Override
    public void from_physical_layer(long time, Frame frame) {
        sim.Log(time + " Frame received: " + frame.toString() + "\n");
        
        
        //  Lidar com as tramas da classe DATA -> Simplex_snd
        if (frame.kind() == Frame.DATA_FRAME) {         //  Check the frame kind -> DATA 
            if (frame.seq() == frame_expected) {        //  Check the sequence number 
                net.to_network_layer(frame.info());     //  Send the frame to the network layer
                sim.start_ack_timer();                  //  Starts an ACK timer to wait for the transmission of a data frame before sending the ACK
                                                        //  o metodo handle_ack_timer vai ser executado 
                                                        //  Adia o envio da trama ACK, à espera que surja uma trama de dados para enviar antes do envio da trama ACK
                frame_expected = next_seq(frame_expected);
            }
        }

        //  Lidar com as tramas da classe ACK -> Simplex_rcv
        if(frame.kind() == Frame.ACK_FRAME){                            //  Se a trama é do tipo ACK
            if(frame.ack() == prev_seq(next_frame_to_send)){
                //  Se receber ACK que confirma que o frame foi recebido, envia proximo packet
                sim.cancel_data_timer(frame.ack());    //  Lidar com o expirar do timer 
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
            sim.start_data_timer(next_frame_to_send);                     //  Start a timer for delay, o metodo handle_data é usado a seguir a esta instrução
           
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
                next_frame_to_send = next_seq(next_frame_to_send);                                                                  //   Incrementa o next_frame_to_send
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
    
    private int frame_expected;
    
    private String last_packet;
    
    /**
     * Reference to the simulator (Terminal), to get the configuration and send commands
     */
    //final Simulator sim;  -  Inherited from Base_Protocol
    
    /**
     * Reference to the network layer, to send a receive packets
     */
    //final NetworkLayer net;    -  Inherited from Base_Protocol
    }
