package purge;

/**
 * 整合voltdb的sql函数，以便于后续产生清理服务用的sql，该类中所有与voltdb时间有关的方法只支持timestamp，
 * 由于voltdb本身不存在把long转化成timestamp的系统函数，所以切记不要提供long。
 * 本类只是通过一些常用函数产生sql语句，并不能保证语句的正确性，如果用户提供不存在的元素或者格式不正确的列，
 * 在voltdb调用该sql语句时，依然会报错
 * Created by swqsh on 2017/6/26.
 */
public class SQLGenerator {

    public static String greater(String expression,String value){
        return expression+" > "+value;
    }

    public static String smaller(String expression,String value){
        return expression+" < "+value;
    }

    public static String equal(String expression,String value){
        return expression+" = "+value;
    }

    public static String abs(String expression){
        return "abs( "+expression+" )";
    }

    public static String and(String expression1,String expression2){
        return expression1+" and "+expression2;
    }

    public static String or(String expression1,String expression2){
        return expression1+" or "+expression2;
    }

    /**
     * 从一个timestamp格式的列中获取其年份，不支持bigint类型
     * @param columnName    voltdb表中的列名
     * @return
     */
    public static String yearOf(String columnName){
        return "year("+columnName+")";
    }

    public static String monthOf(String columnName){
        return "month("+columnName+")";
    }

    public static String dayOf(String columnName){
        return "day("+columnName+")";
    }

    /**
     * timeUnit只支持second、micros和millis，不支持minute、day、month和year等等
     * @param timeUnit
     * @param value
     * @return
     */
    public static String elderThan(String timeUnit,String value) {
        String res="since_epoch(?,now) - since_epoch(?,"+value+")";
        switch (timeUnit){
            case "second":
                res.replace("?","second");
                break;
            case "micros":
                res.replace("?","micros");
                break;
            case "millis":
                res.replace("?","millis");
                break;
            default:
                break;
        }
        return res;
    }
}