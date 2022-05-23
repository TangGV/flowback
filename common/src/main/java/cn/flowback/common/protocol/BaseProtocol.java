package cn.flowback.common.protocol;

/**
 * @author 唐警威
 **/
public abstract class BaseProtocol {

    Integer messageType;

    private BaseProtocol(){};

    public BaseProtocol(Integer messageType){
        this.messageType = messageType;
    }

    public Integer getMessageType() {
        return messageType;
    }

}
