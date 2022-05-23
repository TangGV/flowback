package cn.flowback.common.protocol;

/**
 * 任务节点协议
 *
 * @author 唐警威
 **/
public class WorkNodeProtocol extends BaseProtocol {

    public WorkNodeProtocol() {
        super(MessageType.WORK_NODE);
    }

    private String ip;

    private String cores;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCores() {
        return cores;
    }

    public void setCores(String cores) {
        this.cores = cores;
    }
}
