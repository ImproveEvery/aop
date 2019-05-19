package main.com;

/**
 * Created by Administrator on 2017/11/13 0013.
 */
public enum Type {
    DEFAULT("",""),
    ADD("1","新增"),
    UPDATE("2","修改"),
    DELETE("3","删除")
    ;
    private String id;
    private String operationType;
     Type(String id,String operationType){
        this.id=id;
        this.operationType=operationType;
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getOperationType() {return operationType;}

    public void setOperationType(String operationType) {this.operationType = operationType;}
}
