/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bridging;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

/**
 *
 * @author macbook
 */
@JsonDeserialize(using = ApiResponseDeserializer.class)
public class ResponseModel {
    private boolean success;
    private Integer status;
    private String messageString;
    private List<MessageItem> messageList;
    
    // Getter dan Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getMessageString(){return messageString;}
    public void setMessageString(String message){ this.messageString=message;}

    public List<MessageItem> getMessage() { return messageList; }
    public void setMessage(List<MessageItem> message) { this.messageList = message; }
}
