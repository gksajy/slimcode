package githubcode.slimcode;

/**
 * 存储某一类型代码的起始位置和结束位置,[start,end]，左闭右闭
 */
public class SpanContent {
    public int startWord; // 起始位置
    public int endWord; // 结束位置
    public String content; // 对应内容
    public String preContent;
    public int preCount;

    public int wordCount;


    public SpanContent(int startPos, int endPos, String code) {
//        this.start = start;
//        this.end = end;
        this.preContent = code.substring(0,startPos);
        if (this.preContent.equals("")){
            this.preCount = 0;
        }else {
            this.preCount = preContent.split(" +").length;
        }

        this.content = code.substring(startPos,endPos);
        wordCount = content.split(" +").length;

        this.startWord = preCount;
        this.endWord = preCount + wordCount;
    }

    public static void main(String[] args) {
        String code = "protected final void fastPathOrderedEmit  U value  boolean delayError  Disposable disposable   final Observer   super V  observer  downstream  final SimplePlainQueue  U  q  queue  if ( wip . get ( ) == 0 && wip . compareAndSet ( 0 , 1 ) ) { if ( q . isEmpty ( ) ) { accept ( observer , value ) ; if ( leave ( - 1 ) == 0 ) { return ; } } else { q . offer ( value ) ; } } else { q . offer ( value ) ; if ( ! enter ( ) ) { return ; } } QueueDrainHelper . drainLoop ( q , observer , delayError , disposable , this ) ; } ";
        SpanContent spanContent = new SpanContent(10,19,code);
        System.out.println(spanContent);
    }

}
