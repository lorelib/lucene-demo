package org.lorelib.lucene;

import com.hankcs.lucene.HanLPAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * lucene使用步骤:
 * 1 创建存储目录
 * 2 创建分词器
 * 3 创建索引写入器的配置对象
 * 4 创建索引写入器对象
 * 5 创建文档对象
 * 6 将文档交给索引写入器
 * 7 提交
 * 8 关闭
 */
public class LuceneTest {
    private final static String INDEX_DIRECTORY = "/opt/search/indexDir";

    private static class TextFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(".txt");
        }
    }

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        //1.创建存储目录
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2.创建分词器
        //StandardAnalyzer analyzer = new StandardAnalyzer();
        // Analyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new HanLPAnalyzer();

        //3 创建索引写入器的配置对象
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //4 创建索引写入器对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        //5.创建文档
        Document document1 = new Document();
        //创建字段, TextField既创建索引又会被分词, LongPoint、StringField等只会被索引
        document1.add(new LongPoint("id", 1l));
        document1.add(new StoredField("id", 1L));
        document1.add(new NumericDocValuesField("id", 1L));
        document1.add(new TextField("title", "lucene搜索工具包", Field.Store.YES));
        document1.add(new TextField("content", "lucene is tool", Field.Store.YES));

        Document document2 = new Document();
        //创建字段
        document2.add(new LongPoint("id", 2l));
        document2.add(new StoredField("id", 2L));
        document2.add(new NumericDocValuesField("id", 2L));
        document2.add(new TextField("title", "solr基于lucene的搜索引擎", Field.Store.YES));
        document2.add(new TextField("content", "solr企业级搜索引擎", Field.Store.YES));

        Document document3 = new Document();
        //创建字段
        document3.add(new LongPoint("id", 3l));
        document3.add(new StoredField("id", 3L));
        document3.add(new NumericDocValuesField("id", 3L));
        TextField field = new TextField("title", "elasticsearch非常流行的基于lucene的搜索引擎", Field.Store.YES);
        document3.add(field);
        document3.add(new TextField("content", "elasticsearch is very good", Field.Store.YES));

        Document document4 = new Document();
        //创建字段
        document4.add(new LongPoint("id", 4l));
        document4.add(new StoredField("id", 4L));
        document4.add(new NumericDocValuesField("id", 4L));
        document4.add(new TextField("title", "elasticsearch VS solr", Field.Store.YES));
        document4.add(new TextField("content", "搜索引擎", Field.Store.YES));

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
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2 创建索引读取工具
        IndexReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //4 创建查询解析器
        // QueryParser parser = new QueryParser("content", new SmartChineseAnalyzer());
        QueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content"}, new SmartChineseAnalyzer());

        //5 创建查询对象
        Query query = parser.parse("搜索引擎");

        //6 搜索数据
        TopDocs topDocs = searcher.search(query, 10);

        //7 各种操作
        long totalHits = topDocs.totalHits.value;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        System.out.println("总共找到条数：" + totalHits);

        for (ScoreDoc sd : scoreDocs) {
            //文档编号
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id") + "\ttitle：" + document.get("title") + "\tcontent: " + document.get("content") + "\t得分:" + sd.score);
        }
    }

    /**
     * 搜索工具类
     * @param query
     * @throws Exception
     */
    public void search(Query query) throws Exception {
        //1 创建读取目录对象
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2 创建索引读取工具
        IndexReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //6 搜索数据
        TopDocs topDocs = searcher.search(query, 10);

        //7 各种操作
        long totalHits = topDocs.totalHits.value;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        System.out.println("总共找到条数：" + totalHits);

        for (ScoreDoc sd : scoreDocs) {
            //文档编号
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id") + "\ttitle：" + document.get("title") + "\tcontent: " + document.get("content") + "\t得分:" + sd.score);
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
    public void testNumericRangeQuery() throws Exception {
        Query query = LongPoint.newRangeQuery("id", 2l, 3l);
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
        Query query = LongPoint.newRangeQuery("id", 1l, 3l);
        Query query2 = LongPoint.newRangeQuery("id", 2l, 4l);

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST_NOT);
        search(booleanQuery.build());
    }

    /**
     * 短语查询,使用SmartChineseAnalyzer查不出结果，这与分词器有关，使用HanLPAnalyzer则能查出结果
     */
    @Test
    public void testPhraseQuery() throws Exception {
        Query query = new PhraseQuery(5, "title", new String[]{"elasticsearch", "lucene"});
        search(query);
    }

    /**
     * 排序,lucene默认是按评分排序的，如果设置了排序字段就不按评分排序了，如果还需要优先按评分排序
     * 若没有对id使用NumericDocValuesField，则报错：java.lang.IllegalStateException: unexpected docvalues type NONE for field 'id' (expected=NUMERIC). Re-index with correct docvalues type.
     **/
    @Test
    public void testSortQuery() throws Exception {
        FSDirectory directory = FSDirectory.open(new File(INDEX_DIRECTORY).toPath());

        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("title", new SmartChineseAnalyzer());
        Query query = parser.parse("搜索引擎");
        // Query query = new MatchAllDocsQuery(); // 查询所有
        // Query query = new FuzzyQuery(new Term("content", "搜索引擎")); // text:搜索，将会搜索出没有包含"搜索"文字的数据，因为FuzzyQuery容忍2个字的误差
        // Query query = new WildcardQuery(new Term("title", "*搜索引擎*"));

        // 升序
        // Sort sort = new Sort(new SortField(null, SortField.Type.SCORE, true), new SortField("id", SortField.Type.LONG, false));
        // 降序
        Sort sort = new Sort(SortField.FIELD_SCORE, new SortField("id", SortField.Type.LONG, false));

        // TopDocs search = searcher.search(query, 10, sort); // 加了sort后取不到评分
        TopDocs search = searcher.search(query, 10, sort, true); // 设置doDocScores为true就能得到评分

        long totalHits = search.totalHits.value;
        System.out.println("total:" + totalHits);

        ScoreDoc[] scoreDocs = search.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id") + "\ttitle:" + document.get("title") + "\tcontent: " + document.get("content") + "\t得分:" + sd.score);
        }
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
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2 创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());

        //3 创建索引写入器
        IndexWriter writer = new IndexWriter(directory, config);

        //4 创建文档数据
        Document document = new Document();
        document.add(new LongPoint("id", 2l));
        document.add(new StoredField("id", 2L));
        document.add(new NumericDocValuesField("id", 2L));
        document.add(new TextField("title", "solr基于lucene的搜索引擎dagasdga", Field.Store.YES));
        document.add(new TextField("content", "solr企业级搜索引擎asgasdga", Field.Store.YES));

        //5 修改
        // writer.updateDocument(new Term("id", "1"), document);
        writer.deleteDocuments(LongPoint.newRangeQuery("id", 2l, 2l));
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
        FSDirectory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2 创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());

        //3 创建索引写入器
        IndexWriter writer = new IndexWriter(directory, config);

        //4 删除
        //根据词条进行删除
        writer.deleteDocuments(new Term("id", "1"));

        //根据query对象删除,如果id是数值可以进行数值范围锁定一个具体的id
        /*Query query = LongPoint.newRangeQuery("id", 2l, 3l);
        writer.deleteDocuments(query);*/
        //删除所有
        // writer.deleteAll();

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
        FSDirectory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        //2 创建目录读取器
        DirectoryReader reader = DirectoryReader.open(directory);

        //3 创建索引搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        //4 创建查询解析器
        QueryParser parser = new QueryParser("title", new SmartChineseAnalyzer());

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
        System.err.println("本次搜索到：" + topDocs.totalHits.value + "条数据");

        //10 获取结果
        ScoreDoc[] docs = topDocs.scoreDocs;
        for (ScoreDoc sd : docs) {
            int doc = sd.doc;
            Document document = reader.document(doc);
            System.out.println("id:" + document.get("id"));
            //11 用高亮工具处理普通的查询结果
            String title = document.get("title");
            String hTitle = highlighter.getBestFragment(new SmartChineseAnalyzer(), "title", title);
            System.out.println("title：" + hTitle);
            System.out.println("得分：" + sd.score);
        }
    }

    /**
     * lucene没有提供分页
     **/
    @Test
    public void testPage() throws IOException, ParseException {
        int pageSize = 1;
        int pageNum = 1;
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize;

        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("title", new SmartChineseAnalyzer());
        Query query = parser.parse("搜索");
        Sort sort = new Sort(new SortField("id", SortField.Type.LONG, false));
        TopFieldDocs topDocs = searcher.search(query, end, sort);
        System.out.println("本次总共搜索到了" + topDocs.totalHits.value);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            int doc = scoreDoc.doc;
            Document document = reader.document(doc);

            System.out.println("id: " + document.get("id") + " title\t" + document.get("title"));
        }
    }
}
