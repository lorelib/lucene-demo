package org.lorelib.lucene;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

/**
 * lucene使用步骤:
 * 1 创建文档对象
 * 2 创建存储目录
 * 3 创建分词器
 * 4 创建索引写入器的配置对象
 * 5 创建索引写入器对象
 * 6 将文档交给索引写入器
 * 7 提交
 * 8 关闭
 */
public class LuceneTest {
    private final static String INDEX_DIRECTORY = "/opt/search/indexDir";

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        //1.创建文档
        Document document1 = new Document();
        //创建字段, TextField既创建索引又会被分词, LongField、StringField等只会被索引
        document1.add(new LongField("id", 1l, Field.Store.YES));
        document1.add(new TextField("title", "lucene搜索工具包", Field.Store.YES));

        Document document2 = new Document();
        //创建字段
        document2.add(new LongField("id", 2l, Field.Store.YES));
        document2.add(new TextField("title", "solr基于lucene的搜索引擎", Field.Store.YES));

        Document document3 = new Document();
        //创建字段
        document3.add(new LongField("id", 3l, Field.Store.YES));
        TextField field = new TextField("title", "elasticsearch非常流行的基于lucene的搜索引擎", Field.Store.YES);
        /*如果希望某些文档或域比其他的域更重要，如果此文档或域包含所要查询的词则应该得分较高，则可以在索引阶段设定文档或域的boost值。
        这些值是在索引阶段就写入索引文件的，存储在标准化因子(.nrm)文件中，一旦设定，除非删除此文档，否则无法改变。
        如果不进行设定，则Document Boost和Field Boost默认为1。*/
        field.setBoost(10);
        document3.add(field);

        Document document4 = new Document();
        //创建字段
        document4.add(new LongField("id", 4l, Field.Store.YES));
        document4.add(new TextField("title", "elasticsearch VS solr", Field.Store.YES));

        //2.创建存储目录
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //3.创建分词器
        //StandardAnalyzer analyzer = new StandardAnalyzer();
        IKAnalyzer analyzer = new IKAnalyzer();

        //4 创建索引写入器的配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);

        //5 创建索引写入器对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        //6.把文档交给IndexWriter
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.addDocument(document4);

        //7.提交
        indexWriter.commit();

        //8.关闭
        indexWriter.close();
        System.out.println("it is done!.....");
    }

    /**
     * 搜索
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSearch() throws IOException, ParseException {
        //1 创建读取目录对象
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //2 创建索引读取工具
        IndexReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //4 创建查询解析器
        QueryParser parser = new QueryParser("title", new IKAnalyzer());

        //5 创建查询对象
        Query query = parser.parse("elasticsearch");

        //6 搜索数据
        TopDocs topDocs = searcher.search(query, 10);

        //7 各种操作
        int totalHits = topDocs.totalHits;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        System.out.println("总共找到条数：" + totalHits);

        for (ScoreDoc sd : scoreDocs) {
            //文档编号
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id" + document.get("id") + "\ttitle：" + document.get("title") + "\t得分:" + sd.score);
        }
    }

    /**
     * 搜索工具类
     * @param query
     * @throws Exception
     */
    public void search(Query query) throws Exception {
        //1 创建读取目录对象
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //2 创建索引读取工具
        IndexReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //6 搜索数据
        TopDocs topDocs = searcher.search(query, 10);

        //7 各种操作
        int totalHits = topDocs.totalHits;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        System.out.println("总共找到条数：" + totalHits);

        for (ScoreDoc sd : scoreDocs) {
            //文档编号
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id" + document.get("id") + "\ttitle：" + document.get("title") + "\t得分:" + sd.score);
        }
    }

    /**
     * 词条搜索，词条是数据分词后得到的每一个词，不能再继续分。值必须是字符串！
     * @throws Exception
     */
    @Test
    public void testTermQuery() throws Exception {
        //创建词条查询对象
        Query query = new TermQuery(new Term("title", "搜索"));
        search(query);
    }

    /**
     * 通配符搜索
     *  ? 可以代表任意一个字符
     * 	* 可以任意多个任意字符
     * @throws Exception
     */
    @Test
    public void testWildcardQuery() throws Exception {
        Query query = new WildcardQuery(new Term("title", "*solr*"));
        search(query);
    }

    /**
     * 模糊搜索
     * 允许用户输错，但是要求错误的最大编辑距离不能超过2
     * 编辑距离：一个单词到另一个单词最少要修改的次数 facebool --> facebook 需要编辑1次，编辑距离就是1
     * 可以手动指定编辑距离，但是参数必须在0~2之间
     * @throws Exception
     */
    @Test
    public void testFuzzyQuery() throws Exception {
        Query query = new FuzzyQuery(new Term("title", "elasicseach"));
        search(query);

        query = new FuzzyQuery(new Term("title", "elasicseach"), 1);
        search(query);
    }

    /**
     * 数值范围搜索
     * @throws Exception
     */
    @Test
    public void NumericRangeQuery() throws Exception {
        Query query = NumericRangeQuery.newLongRange("id", 2l, 3l, true, true);
        search(query);
    }

    /**
     * 布尔搜索本身没有查询条件，使用其它查询再通过逻辑运算进行组合
     * 交集：Occur.MUST + Occur.MUST
     * 并集：Occur.SHOULD + Occur.SHOULD
     * 非：Occur.MUST_NOT
     */
    @Test
    public void testBooleanQuery() throws Exception {
        //区间查询
        Query query = NumericRangeQuery.newLongRange("id", 1l, 3l, true, true);
        Query query2 = NumericRangeQuery.newLongRange("id", 2l, 4l, true, true);

        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(query, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST_NOT);
        search(booleanQuery);
    }

    /**
     * 更新索引
     * 注意：
     * 	A：Lucene修改功能底层会先删除，再把新的文档添加。
     * 	B：修改功能会根据Term进行匹配，所有匹配到的都会被删除。这样不好
     * 	C：因此，一般我们修改时，都会根据一个唯一不重复字段进行匹配修改。例如ID
     * 	D：但是词条搜索，要求ID必须是字符串。如果不是，这个方法就不能用。
     *     如果ID是数值类型，我们不能直接去修改。可以先手动删除deleteDocuments(数值范围查询锁定ID)，再添加。
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        //1 创建读取目录对象
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //2 创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());

        //3 创建索引写入器
        IndexWriter writer = new IndexWriter(directory, config);

        //4 创建文档数据
        Document document = new Document();
        document.add(new LongField("id", 2l, Field.Store.YES));
        document.add(new TextField("title", "solr基于lucene的搜索引擎1232525", Field.Store.YES));

        //5 修改
        // writer.updateDocument(new Term("id", "1"), document);
        writer.deleteDocuments(NumericRangeQuery.newLongRange("id", 2l, 2l, true, true));
        writer.updateDocument(new Term("id", "2"), document);

        //6 提交
        writer.commit();

        //7 关闭
        writer.close();
    }

    /**
     * 删除索引
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        //1 创建文档对象目录
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //2 创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());

        //3 创建索引写入器
        IndexWriter writer = new IndexWriter(directory, config);

        //4 删除
        //根据词条进行删除
        // writer.deleteDocuments(new Term("id", "1"));

        //根据query对象删除,如果id是数值可以进行数值范围锁定一个具体的id
        /*Query query = NumericRangeQuery.newLongRange("id", 1l, 2l, true, true);
        writer.deleteDocuments(query);*/
        //删除所有
        writer.deleteAll();

        //5 提交
        writer.commit();
        //6 关闭
        writer.close();
    }

    /**
     * 高亮显示
     * @throws Exception
     */
    @Test
    public void testHighlighter() throws Exception {
        //1 创建目录对象
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        //2 创建目录读取器
        DirectoryReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //4 创建查询解析器
        QueryParser parser = new QueryParser("title", new IKAnalyzer());

        //5 创建查询对象
        Query query = parser.parse("搜索");

        //6 创建格式化器
        Formatter formatter = new SimpleHTMLFormatter("<em>", "</em>");

        //7 创建查询分数工具
        Scorer scorer = new QueryScorer(query);

        //8 准备高亮工具
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //9 搜索
        TopDocs topDocs = searcher.search(query, 10);
        System.err.println("本次搜索到：" + topDocs.totalHits + "条数据");

        //10 获取结果
        ScoreDoc[] docs = topDocs.scoreDocs;
        for (ScoreDoc sd : docs) {
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id"));
            //11 用高亮工具处理普通的查询结果
            String title = document.get("title");
            String hTitle = highlighter.getBestFragment(new IKAnalyzer(), "title", title);
            System.out.println("title：" + hTitle);
            System.out.println("得分：" + sd.score);
        }
    }

    /**
     * 排序
     **/
    @Test
    public void testSortQuery() throws Exception {
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));

        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("title", new IKAnalyzer());

        Query query = parser.parse("搜索");

        Sort sort = new Sort(new SortField("id", SortField.Type.LONG, false));

        TopFieldDocs search = searcher.search(query, 10, sort);

        int totalHits = search.totalHits;
        System.out.println("total:" + totalHits);

        ScoreDoc[] scoreDocs = search.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id") + "\ttitle:" + document.get("title"));
        }
    }

    /**
     * lucene没有提供分页
     **/
    @Test
    public void testPage() throws IOException, ParseException {
        int pageSize = 2;
        int pageNum = 2;
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize;

        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("搜索");
        Sort sort = new Sort(new SortField("id", SortField.Type.LONG, false));
        TopFieldDocs topDocs = searcher.search(query, end, sort);
        System.out.println("本次总共搜索到了" + topDocs.totalHits);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            int doc = scoreDoc.doc;
            Document document = reader.document(doc);

            System.out.println("id\t" + document.get("id") + "title\t" + document.get("title"));
        }
    }
}
