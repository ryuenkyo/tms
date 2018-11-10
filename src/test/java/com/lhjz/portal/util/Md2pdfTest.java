package com.lhjz.portal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.qkyrie.markdown2pdf.Markdown2PdfConverter;
import com.qkyrie.markdown2pdf.internal.exceptions.ConversionException;
import com.qkyrie.markdown2pdf.internal.exceptions.Markdown2PdfLogicException;
import com.qkyrie.markdown2pdf.internal.reading.SimpleStringMarkdown2PdfReader;
import com.qkyrie.markdown2pdf.internal.writing.SimpleFileMarkdown2PdfWriter;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;

public class Md2pdfTest {

	public static void main(String[] args) throws IOException {

		String path = new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();
		String pathMd = path + "/test.md";
		String pathPdf = path + "/test.pdf";
		System.out.println(path);
		System.out.println("Start create md(" + pathMd + ")...");
		FileUtils.writeStringToFile(new File(pathMd), "## Markdown title\n> test...", "UTF-8");
		System.out.println("End create md(" + pathMd + ")...");
		// System.out.println(path);
		// System.out.println(pathMd);
		// System.out.println(pathPdf);
		String nodeCmd = StringUtil.replace("node {?1} {?2} {?3}", path, new File(pathMd).getAbsolutePath(),
				new File(pathPdf).getAbsolutePath());
		System.out.println(nodeCmd);
		try {
			Process process = Runtime.getRuntime().exec(nodeCmd);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s = null;
			while ((s = bufferedReader.readLine()) != null) {
				System.out.println(s);
			}
			process.waitFor();
			System.out.println("Md2pdf done!");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void basicExample() throws Markdown2PdfLogicException, ConversionException, IOException {

		/*
		    Let's create an instance of the markdown2PdfConverter to work with
		 */
		Markdown2PdfConverter markdown2PdfConverter = Markdown2PdfConverter.newConverter();

		/*
		    We'll have to specify where our converter has to get his markdown data.
		    With Java 8 in mind, we chose for a more generic approach, where we only have to
		    implement a class defined by Markdown2PdfReader
		 */

		String md = FileUtils.readFileToString(new File("/Users/xiweicheng/temp/test123.md"), "utf-8");
		markdown2PdfConverter.readFrom(new SimpleStringMarkdown2PdfReader(md));

		/*
		    Same thing goes for our writer. When the reading, cleaning and converting is done,
		    we need to do something with the resulted bytes. What you do with it is entirely up to you.
		 */
		//		markdown2PdfConverter.writeTo(out -> {
		//			//do something with it here
		//		});

		markdown2PdfConverter.writeTo(new SimpleFileMarkdown2PdfWriter(new File("/Users/xiweicheng/temp/test123.pdf")));

		/*
		    What has the API done at this point? The simple answer: nothing. It only set the states and placeholders
		    for what it will be doing. It's lazily executed, which means that you'll have to explicitly make it do the
		    work in order for it to read, clean and convert.
		
		    Actually making the API do the work can be done as follows:
		 */
		markdown2PdfConverter.doIt();
	}

	/**
	 * Our API has been implemented in such a way, that you can use it as a oneliner.
	 * The above example can be rewritten, as shown in the this example
	 */
	@Test
	public void basicExampleAsOneLiner() throws Markdown2PdfLogicException, ConversionException {
		Markdown2PdfConverter.newConverter().readFrom(() -> "***Test***").writeTo(out -> {
			//here you can just do something with the bytes, like write it to a file
			//for example.
		}).doIt();
	}

	/**
	 * It might seem tedious, implementing two interfaces in order to make the execution work, but believe us when
	 * we say that it's for the best. With Java 8 in mind, these interfaces will be functional interfaces, and will be
	 * replaceable with Lambda expressions.
	 *
	 * Fortunately, we provided some Simple implementations which can serve as replacement
	 * for your own implementation of the interfaces. Just follow the
	 */
	@Test
	public void additionalUtilityClasses() {
		utilityReaders();
		utilityWriters();
	}

	/**
	 * UtilityReaders are simple implementations of the
	 * Markdown2PdfReader interface
	 */
	private void utilityReaders() {
		simpleStringMarkdown2PdfReader();
	}

	/**
	 * UtilityWriters on the other hand are simple implementation
	 * of the Markdown2PdfWriter interface
	 */
	private void utilityWriters() {
		simpleFileMarkdown2PdfWriter();
	}

	/**
	 * the SimpleStringMarkdown2PdfReader can be used to read from a String. It's the most simple
	 * of implementations, which you'll probably find using a lot.
	 */
	private void simpleStringMarkdown2PdfReader() {
		Markdown2PdfConverter.newConverter().readFrom(new SimpleStringMarkdown2PdfReader("***Test***"));
	}

	/**
	 * The SimpleFileMarkdown2PdfWriter implements the Markdown2PdfWriter in such a way, that the only
	 * thing you need to provide is a file. It will write the resulted bytes to the file.
	 */
	private void simpleFileMarkdown2PdfWriter() {
		Markdown2PdfConverter.newConverter()
				.writeTo(new SimpleFileMarkdown2PdfWriter(new File("/Users/xiweicheng/temp/test123.pdf")));
	}

	static final MutableDataHolder OPTIONS = PegdownOptionsAdapter
			.flexmarkOptions(Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)).toMutable();

	// https://github.com/vsch/flexmark-java
	@Test
	public void test() throws IOException {
		MutableDataSet options = new MutableDataSet();

		// uncomment to set optional extensions
		options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		// You can re-use parser and renderer instances
		String md = FileUtils.readFileToString(new File("/Users/xiweicheng/temp/test123.md"));
		Node document = parser.parse(md);
		String html = renderer.render(document); // "<p>This is <em>Sparta</em></p>\n"
		System.out.println(html);

		//		OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream("/Users/xiweicheng/temp/flexmark-java.pdf"), "utf-8");

		PdfConverterExtension.exportToPdf(new FileOutputStream("/Users/xiweicheng/temp/flexmark-java.pdf"), html, "",
				OPTIONS);

	}
}
