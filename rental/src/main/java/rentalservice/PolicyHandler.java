package rentalservice;

import rentalservice.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayConfirmed_UpdateStatus(@Payload PayConfirmed payConfirmed){

        if(payConfirmed.isMe()){
            System.out.println("##### listener UpdateStatus : " + payConfirmed.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReclamationConfirmed_UpdateStatus(@Payload ReclamationConfirmed reclamationConfirmed){

        if(reclamationConfirmed.isMe()){
            System.out.println("##### listener UpdateStatus : " + reclamationConfirmed.toJson());
        }
    }

}
