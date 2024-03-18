package gesser.gals;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.UIManager;

/*
 * TODO: Problema de contexto sem nada depois
 * TODO: Simulador Passo a Passo
 * TODO: Comprimir Tabelas Léxico - C++ 
 * TODO: Comprimir Tabelas Léxico - Delphi
 * TODO: Comprimir Tabelas Sintático - Java
 * TODO: Comprimir Tabelas Sintático - C++
 * TODO: Comprimir Tabelas Sintático - Delphi
 * TODO: Mostrar AF em modo gráfico
 * TODO: Melhorar msgs de erro para (S|LA)?LR
 * TODO: Eliminar Calculo repedido do AF (qd sintatico pega lista de tokens)
 * TODO: Reabilitar transformações
 */

public class GALS
{
    public static void centralize(Component c)
    {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Point center = new Point(d.width/2, d.height/2);
        c.setLocation(center.x-c.getWidth()/2, center.y-c.getHeight()/2);
    }
    
    public static void main(String[] args)
    {
        try
        {
            /*
            com.incors.plaf.kunststoff.KunststoffLookAndFeel kunststoffLnF = new com.incors.plaf.kunststoff.KunststoffLookAndFeel();
            KunststoffLookAndFeel.setCurrentTheme(new com.incors.plaf.kunststoff.KunststoffTheme());
            //UIManager.setLookAndFeel(kunststoffLnF);
             */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        MainWindow window = MainWindow.getInstance();
        centralize(window);
   
        window.show();
    }
}
