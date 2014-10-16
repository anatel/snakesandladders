/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import model.Game;

/**
 *
 * @author Anat
 */
public class RetirePlayerUtil implements Runnable{

    ServletContext servletContext;
    Game currGame;
    String playerLeftName;
    QuitPlayer qp;
    
    public RetirePlayerUtil(ServletContext servletContext, Game currGame,
                            QuitPlayer qp)
    {
        this.servletContext = servletContext;
        this.currGame = currGame;
        this.playerLeftName = currGame.getCurrPlayer().getPlayerName();
        this.qp = qp;
    }
    
    @Override
    public void run() {
        //remove current player from game
        //update the servlet context and the quit id        
        //has any player left will ask if the session is the one who left and if so, will delete the session
        //and return that that he's the one who left and show message on session clien side.
        ServletUtils.retirePlayerFromGame(servletContext, currGame, playerLeftName, qp);
        ServletUtils.retirePlayerSpecialChecks(servletContext, currGame, qp);
    }
}
