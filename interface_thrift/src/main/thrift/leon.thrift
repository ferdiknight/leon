namespace java com.blueferdi.leon.typehead.index.construct

struct TypeaheadElement
{
    1:i64 id,//对象唯一标识
    2:string name,//对象名称
    3:i64 timestamp,//对象创建时间
    4:list<string> term_contents,//需要建立索引的字段值集合
    5:map<string,string> attrs,//其他用于前台显示的属性
    6:double score,//可自定义权重值
    7:list<string> terms,//自定义输入索引分词集合
    8:bool needDefaultTermer,//是否使用默认分词器分析需索引字段
    9:string type
}

struct TypeaheadElementSet
{
    1:bool hasNext,//标识遍历是否结束
    2:string position,//标识遍历集合当前位置
    3:list<TypeaheadElement> sets//数据集合
}

//索引构建器
service IndexConstructor
{
    //执行数据遍历
    TypeaheadElementSet execute
    (
        1:string position,//前一次获取数据的集合位置 
        2:i32 length//本次获取数据的长度
    )
}