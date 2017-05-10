import java.io.BufferedReader;
import java.io.IOException;

/**
 * 
 * @author Ellie
 * @date 2017/5/1
 * 从控制台输入流中读入语句
 *
 */
public class Source
{
    // 行结束符，注意在Windows平台上，默认行结束符是\r\n，
    //如果用记事本之类的写的pascal源程序，可以使用Ultraedit之类的给转成Unix格式的。
    public static final char EOL = '\n';     
    //文件结束标识
    public static final char EOF = (char) 0;  
    //源程序reader
    private final BufferedReader reader;
    private String line;                                   
    private int currentPos;                   // 当前行相对位置，不是整个文件的offset！！
    public Source(BufferedReader reader)
        throws IOException
    {
        this.currentPos = -2;  // 设置为-2表示文件一行都没有读，后面的判断可以根据是否等于-2读文件第一行。
        this.reader = reader;

    }
    
    public int getPosition(){
    	return this.currentPos;
    }

    /**
     * @return 要去读的字符
     * @throws Exception(read过程中的异常)
     */
    public char currentChar()
        throws Exception
    {
        // 第一次读?
        if (currentPos == -2) {
            readLine();
            return nextChar();
        }
        // 文件结束?
        else if (line == null) {
            return EOF;
        }
        // 行结束?
        else if ((currentPos == -1) || (currentPos == line.length())) {
            return EOL;
        }
        // 超过一行，换一行再读
        else if (currentPos > line.length()) {
            readLine();
            return nextChar();
        }
        // 正常读取当前行的某一列的字符
        else {
            return line.charAt(currentPos);
        }
    }

    /**
     *位置游标前进一步并返回对应的字符，记住source的位置游标<b>从来不后退，只有向前操作。</b>
     * @return 下一个要读取的字符
     * @throws Exception
     */
    public char nextChar()
        throws Exception
    {
        ++currentPos;
        return currentChar();
    }

    /**
     * 探测下一字符，位置游标不增加，跟Stack（栈）的Peek方法一样效果。
     * @return 当前位置的字符
     * @throws Exception 
     */
    public char peekChar()
        throws Exception
    {
        currentChar();
        if (line == null) {
            return EOF;
        }
 
        int nextPos = currentPos + 1;
        return nextPos < line.length() ? line.charAt(nextPos) : EOL;
    }

    /**
     * 读入一行
     * @throws IOException
     */
    private String readLine()
        throws IOException
    {
        line = reader.readLine(); 
        currentPos = -1;
        //如果读成功，行数+1
        if (line != null) {
            return "SUCCESS";
        }else{
        	return null;
        }
    }
    
    public void close()
        throws Exception
    {
        if (reader != null) {
            try {
                reader.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }
}