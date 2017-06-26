package purge;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 清理服务清除规则的抽象类，目前规则总共有{@link SQLRule}和{@link PurgeVoltProcedure}两类，
 * 该抽象类的作用主要是用来实现错误管理，在调用失败达3次或以上时，应该给予移除
 * Created by swqsh on 2017/6/26.
 */
public abstract class Rule {

    //最大失败次数
    private static final int MAX_FAILURE_TIMES=3;
    //该存储过程当前失败次数
    private AtomicInteger failTimes=new AtomicInteger(0);

    /**
     * 清理服务在调用存储过程出现异常时，调用此方法
     */
    public void fail(){
        failTimes.incrementAndGet();
    }

    /**
     * 每次调用存储过程成功的情况下，都应该重置失败次数
     */
    public void reset(){
        failTimes.set(0);
    }

    public boolean shouldBeRemoved(){
        if(failTimes.get()>=MAX_FAILURE_TIMES)
            return true;
        else
            return false;
    }

}
