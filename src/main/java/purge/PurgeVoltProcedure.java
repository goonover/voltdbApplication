package purge;

/**
 *存储Voltdb存储过程被调用所需数据
 * Created by swqsh on 2017/6/22.
 */
public class PurgeVoltProcedure {

    private String procedureName;
    private Object[] params;

    //构造器，通过string生成PurgeVoltProcedure，方便解析以及网络传输
    public static PurgeVoltProcedure parseProcedureFromStr(String src){
        String[] temp=src.split("\\s+");
        if(temp.length==0)
            return null;
        PurgeVoltProcedure procedure=new PurgeVoltProcedure();
        String procedureName=temp[0];
        Object[] params=new Object[temp.length-1];
        for(int i=1;i<temp.length;i++){
            params[i-1]=temp[i];
        }
        procedure.setProcedureName(procedureName);
        procedure.setParams(params);
        return procedure;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    //主要方便回写到文档之中
    public String toString(){
        String res=procedureName;
        for(Object param:params){
            res+=" "+(String)param;
        }
        return res;
    }

    @Override
    public boolean equals(Object s){
        if(!(s instanceof PurgeVoltProcedure))
            return false;

        PurgeVoltProcedure procedure=(PurgeVoltProcedure)s;

        if(!(procedure.getProcedureName().equals(procedureName)))
            return false;
        Object[] procedureParams=procedure.getParams();
        if(procedureParams.length!=params.length)
            return false;

        for(int i=0;i<params.length;i++){
            if(!(procedureParams[i].equals(params[i])))
                return false;
        }
        return true;
    }
}
