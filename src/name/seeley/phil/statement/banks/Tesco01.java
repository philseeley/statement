package name.seeley.phil.statement.banks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.seeley.phil.statement.jaxb.Entry;
import name.seeley.phil.statement.jaxb.ObjectFactory;
import name.seeley.phil.statement.Bank;

public class Tesco01 implements Bank
{
  static private final String FILE_EXT = "tsv";
  static private final String BANK_TLA = "TCO";
  
  @Override
  public List<Entry> parse(ObjectFactory factory, File file) throws Exception
  {
    List<Entry> entries = new ArrayList<Entry>();

    BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
      
    r.readLine(); // Discard header comment line
    
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    NumberFormat nf = DecimalFormat.getCurrencyInstance();
    
    String l;
    while((l = r.readLine()) != null)
    {
      String[] t = l.split("\t");

      Date d = df.parse(t[0]);
      
      GregorianCalendar c = new GregorianCalendar();
      c.setTime(d);
      
      XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

      Entry e = factory.createEntry();

      e.setDate(xc);
      e.setDescr(t[1]+" - "+t[2]);
      e.setValue(nf.parse(t[4]).floatValue());
      
      entries.add(e);
    }
    
    return entries;
  }

  @Override
  public String bankTLA()
  {
    return BANK_TLA;
  }

  @Override
  public String description()
  {
    return "Tesco tab separated, v1";
  }

  @Override
  public String extension()
  {
    return FILE_EXT;
  }

  @Override
  public String help()
  {
    return "help/tesco01.html";
  }
}
