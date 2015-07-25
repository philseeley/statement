package name.johnson.kim.statement.banks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
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

public class BigSky01 implements Bank
{
  static private final String FILE_EXT = "csv";
  static private final String BANK_TLA = "BSK";
  
  @Override
  public List<Entry> parse(ObjectFactory factory, File file) throws Exception
  {
    List<Entry> entries = new ArrayList<Entry>();

    BufferedReader r = new BufferedReader(new FileReader(file));
    
    try
    {
      SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
      NumberFormat nf = DecimalFormat.getInstance();
      //read first line and discard as its just the account number     
      r.readLine();
 
      String l;
      while((l = r.readLine()) != null)
      {
        String[] t = l.split(",");
  
        if(t.length >= 4)
        {
          Date d = df.parse(t[0]);
          
          GregorianCalendar c = new GregorianCalendar();
          c.setTime(d);
          
          XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    
          Entry e = factory.createEntry();
    
          e.setDate(xc);
    
          e.setDescr(t[1].substring(1, t[1].length()-1));
          
          String v = t[3];
          
          try
          { 
            e.setValue(nf.parse(v).floatValue()*-1);
    
            entries.add(e);
          }
          catch(ParseException f)
          {
            System.out.println("Unparseable line due to missing value - probably an ATM Enquiry. Entry skipped.");
          }
        }
      }
    }
    finally
    {
      r.close();
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
    return "Big Sky Credit Union comma separated, v1";
  }

  @Override
  public String extension()
  {
    return FILE_EXT;
  }
  
  @Override
  public String help()
  {
    return "help/bigsky01.html";
  }
}
