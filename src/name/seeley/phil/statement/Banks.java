package name.seeley.phil.statement;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Banks
{
  private static final String BANKS_FILENAME = "banks.txt";
  
  private static List<Bank> _banks;
  
  static public List<Bank> getBanks() throws Exception 
  {
    if(_banks == null)
    {
      _banks = new ArrayList<Bank>();
      
      ClassLoader cl = Banks.class.getClassLoader();
  
      InputStream is = cl.getResourceAsStream(BANKS_FILENAME);
      
      if(is == null)
        throw new FileNotFoundException(BANKS_FILENAME);
      
      BufferedReader r = new BufferedReader(new InputStreamReader(is));
      
      String c;
      while((c = r.readLine()) != null)
        _banks.add((Bank) cl.loadClass(c).newInstance());
    }
    
    return _banks;
  }
}
