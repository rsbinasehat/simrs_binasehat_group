/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bridging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author macbook
 */
public class ApiResponseDeserializer extends JsonDeserializer<ResponseModel> {
    
    @Override
    public ResponseModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ResponseModel response = new ResponseModel();
        response.setSuccess(node.get("success").asBoolean());

        if (node.has("status")) {
            response.setStatus(node.get("status").asInt());
        }else{
            response.setStatus(0);
        }

        // message bisa string atau array
        JsonNode messageNode = node.get("message");
        if (messageNode != null) {
            if (messageNode.isTextual()) {
                response.setMessageString(messageNode.asText());
            } else if (messageNode.isArray()) {
                List<MessageItem> items = new ArrayList<>();
                for (JsonNode itemNode : messageNode) {
                    items.add(mapper.treeToValue(itemNode, MessageItem.class));
                }
                response.setMessage(items);
            }
        }

        return response;
    }
}
