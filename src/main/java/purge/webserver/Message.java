package purge.webserver;

/**
 * Created by swqsh on 2017/6/19.
 */
public interface Message {

    public void initFormByteArray(byte[] src);

    public byte[] flattenToByteArray();
}
