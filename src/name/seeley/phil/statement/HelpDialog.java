package name.seeley.phil.statement;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class HelpDialog extends JFrame implements HyperlinkListener
{
  JTextPane  _textPane;
  Stack<URL> _history = new Stack<URL>();
  URL        _url;
  
  public HelpDialog()
  {
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    add(toolBar, BorderLayout.NORTH);
    
    Action a = new AbstractAction("Back", IconUtil.load("go-previous"))
    {public void actionPerformed(ActionEvent e){back();}};
    JButton b = new JButton(a);
    b.setHideActionText(true);
    b.setToolTipText(a.getValue(Action.NAME).toString());
    toolBar.add(b);    

    _textPane = new JTextPane();
    _textPane.setEditable(false);
    _textPane.setContentType("text/html");
    _textPane.addHyperlinkListener(this);

    contentPane.add(new JScrollPane(_textPane), BorderLayout.CENTER);

    setSize(400, 400);
  }

  public void showHelp(String file)
  {
    initHelp(file);
    
    setVisible(true);
  }
  
  private void initHelp(String filename)
  {
    URL url = getClass().getClassLoader().getResource(filename);

    if(url == null)
    {
      System.err.println("Invalid help file '"+filename+"'");
    }
    else
    {
      _history.clear();
    
      setHelp(url);
    }
  }

  private void setHelp(URL url)
  {
    try
    {
      _url = url;

      _textPane.setPage(_url);
    }
    catch (IOException e)
    {
      e.printStackTrace(System.err);
    }
  }

  private void back()
  {
    if(!_history.isEmpty())
      setHelp(_history.pop());
  }
  
  @Override
  public void hyperlinkUpdate(HyperlinkEvent ev)
  {
    if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
    {
      _history.push(_url);
      setHelp(ev.getURL());
    }
  }
}
