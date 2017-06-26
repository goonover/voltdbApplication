package purge;


/**
 * 普通的sql语句由SQLRule包装，不用重新实现失败统计
 * Created by swqsh on 2017/6/26.
 */
public class SQLRule extends Rule {

    private String statement;

    public SQLRule(String statement){
        this.statement=statement;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof SQLRule))
            return false;
        SQLRule rule=(SQLRule) o;
        if(!statement.equals(rule.getStatement()))
            return false;
        return true;
    }

    @Override
    public int hashCode(){
        return statement.hashCode();
    }

    @Override
    public String toString(){
        return statement;
    }

    public String getStatement() {
        return statement;
    }
}
