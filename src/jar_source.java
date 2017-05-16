import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/16.
 */
public class jar_source {
    // 行结束符，注意在Windows平台上，默认行结束符是\r\n，
    //如果用记事本之类的写的pascal源程序，可以使用Ultraedit之类的给转成Unix格式的。
    public static final char EOL = '\n';
    //文件结束标识
    public static final char EOF = (char) 0;
    //源程序reader
    private String string;
    private int currentPos;                   // 当前行相对位置，不是整个文件的offset！！
    public jar_source(String s)
    {
        this.currentPos = 0;  // 设置为-2表示文件一行都没有读，后面的判断可以根据是否等于-2读文件第一行。
        this.string = s;

    }
    public String getString(){
        return this.string;
    }
    public int getPosition(){
        return this.currentPos;
    }

    /**
     * @return 要去读的字符
     * @throws Exception(read过程中的异常)
     */
    public char currentChar()
    {
        return this.string.charAt(currentPos);
    }
    /**
     *位置游标前进一步并返回对应的字符，记住source的位置游标<b>从来不后退，只有向前操作。</b>
     * @return 下一个要读取的字符
     * @throws Exception
     */
    public char nextChar()
    {
        if (currentPos<string.length()-1){
            ++currentPos;
            return currentChar();
        }else{

            return '~';
        }

    }
}
