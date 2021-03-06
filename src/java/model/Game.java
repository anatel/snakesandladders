/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exception.*;
import java.math.BigInteger;
import java.util.*;
import model.Player.LoadedFrom;
import static xmlPackage.PlayerType.*;
import xmlPackage.Snakesandladders;

/**
 *
 * @author Roei
 */
public class Game {

    private String gameName;
    private int m_numSoldiersToWin;
    private int m_numPlayers;
    private GameBoard m_board;
    private LinkedList<Player> playerList;
    private Player currPlayer;
    public static final Integer NUM_SOLDIERS_FOR_EACH_PLAYER = 4;
    private ListIterator<Player> playerItr;
    private Player winner = null;
    private LoadedFrom gameSrc;
    boolean isStarted = false;
    private int joinedCount;
    ReschedulableTimer timer;

    public Game(Snakesandladders gameXml) throws XmlIsInvalidException {
        gameName = gameXml.getName();
        LinkedList<String> playerNames = new LinkedList<String>();
        m_numSoldiersToWin = gameXml.getNumberOfSoldiers();
        m_numPlayers = gameXml.getPlayers().getPlayer().size();
        playerList = new LinkedList<Player>();
        gameSrc = LoadedFrom.XML;
        m_board = new GameBoard(gameXml);
        joinedCount = 0;
        int i = 0;
        

        checkIfXmlGameAlreadyFinished(m_numSoldiersToWin, gameXml.getBoard().getCells().getCell(), gameXml.getBoard().getSize());
        checkNumOfSoldiersXml(gameXml.getBoard().getCells().getCell(), gameXml.getPlayers().getPlayer());

        checkIfAllXmlGamePlayersAreComp(gameXml);

        for (xmlPackage.Players.Player p : gameXml.getPlayers().getPlayer()) {
            if (playerNames.contains(p.getName())) {
                throw new DuplicatePlayerNamesXmlException();
            }
            playerNames.add(i, p.getName());
            if (p.getType() == HUMAN) {
                playerList.add(new HumanPlayer(++i, m_board, gameSrc));
            } else if (p.getType() == COMPUTER) {
                playerList.add(new CompPlayer(++i, p.getName(), m_board, gameSrc));
                joinedCount++;
            }
            playerList.getLast().setPlayerName(playerNames.get(i - 1));
        }

        m_board.setPlayersPosFromXml(gameXml, playerList, playerNames);
        getCurrentPlayerFromXml(gameXml, playerNames);
    }

    public void joinPlayer(String playerName) throws DuplicatePlayerNamesException {
        if (doesPlayerNameAlreadyExistAndJoined(playerName)) {
            throw new DuplicatePlayerNamesException();
        }

        Player newPlayer;

        if (gameSrc == LoadedFrom.REG) {
            newPlayer = getNextPlayerInListStillNotJoined();
            newPlayer.setPlayerName(playerName);
        } else {
            newPlayer = getPlayerByName(playerName);
        }
        newPlayer.setIsJoined(true);
        joinedCount++;
        if (joinedCount == m_numPlayers){
            timer =  new ReschedulableTimer();            
        }
           
    }

    public ReschedulableTimer getTimer()
    {
        return timer;
    }
    private boolean doesPlayerNameAlreadyExistAndJoined(String name) {
        for (Player p : playerList) {
            if (p.getPlayerName() != null) {
                if (p.getPlayerName().equals(name) && p.isJoined()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Player getNextPlayerInListStillNotJoined() {
        for (Player p : playerList) {
            if (!p.isJoined()) {
                return p;
            }
        }
        return null;
    }

    public Player getPlayerByName(String playerName) {
        for (Player p : playerList) {
            if (p.getPlayerName() != null) {
                if (p.getPlayerName().equals(playerName)) {
                    return p;
                }
            }
        }
        return null;
    }

    public Game(int boardSize, int numOfLadders, int numSoldiersToWin, int numPlayers, Player.PlayerType[] playerTypes, String gameName) {

        this.gameName = gameName;
        m_board = new GameBoard(boardSize, numOfLadders, numPlayers);
        m_numSoldiersToWin = numSoldiersToWin;
        m_numPlayers = numPlayers;
        playerList = new LinkedList<>();
        gameSrc = LoadedFrom.REG;

        int i;
        int compInd = 0;
        for (i = 0; i < m_numPlayers; i++) {
            if (playerTypes[i] == Player.PlayerType.COMP) {
                playerList.add(new CompPlayer(i + 1, "Comp" + ++compInd, m_board, gameSrc));
                joinedCount++;
            } else {
                playerList.add(new HumanPlayer(i + 1, m_board, gameSrc));
            }
        }

        int j;
        for (i = 0; i < numPlayers; i++) {
            for (j = 0; j < Player.NUM_SOLDIERS; j++) {
                m_board.getFirstCell().insertSoldier(i + 1);
            }
        }
        currPlayer = playerList.getFirst();
        playerItr = playerList.listIterator();
        setIteratorOnFirstPlayer(playerItr, currPlayer);
    }

    private void setIteratorOnFirstPlayer(Iterator<Player> itr, Player first) {
        Player tmpPlayer = null;
        while (itr.hasNext() && first != tmpPlayer) {
            tmpPlayer = itr.next();
        }
    }

    public Player getWinner() {
        return winner;
    }

    public boolean isWinner(Player player) {
        int lastCell = m_board.getLastCellNum();
        if (m_board.getCell(lastCell).getSoldiersInCell()[player.getPlayerNum() - 1] == m_numSoldiersToWin) {
            winner = player;
            return true;
        } else if (playerList.size() == 1) {
            winner = player;
            return true;
        } else {
            return false;
        }
    }

    public void advanceTurnToNextPlayer() {
        if (playerItr.hasNext()) {
            setCurrPlayer(playerItr.next());
        } else {
            setCurrPlayer(playerList.getFirst());
            playerItr = playerList.listIterator();
            if (playerItr.hasNext()) {
                playerItr.next();
            }
        }

    }

    public int getPlayerNumByName(String playerName) {
        int res = 0;

        for (int i = 0; i < playerList.size(); i++) {
            if (playerList.get(i).getPlayerName() != null) {
                if (playerList.get(i).getPlayerName().equals(playerName)) {
                    res = playerList.get(i).getPlayerNum();
                }
            }
        }

        return res;
    }

    public void setCurrPlayer(Player curr) {
        this.currPlayer = curr;
    }

    public void checkNumOfSoldiersXml(List<xmlPackage.Cell> xmlCells, List<xmlPackage.Players.Player> playerListXml) throws XmlIsInvalidException {
        HashMap<String, Integer> numSoldiers = new HashMap();
        for (xmlPackage.Players.Player p : playerListXml) {
            numSoldiers.put(p.getName(), m_board.EMPTY);
        }

        for (xmlPackage.Cell c : xmlCells) {
            for (xmlPackage.Cell.Soldiers s : c.getSoldiers()) {
                numSoldiers.put(s.getPlayerName(), numSoldiers.get(s.getPlayerName()) + s.getCount());
            }
        }

        for (Map.Entry<String, Integer> entry
                : numSoldiers.entrySet()) {
            if (entry.getValue() != Player.NUM_SOLDIERS) {
                throw new PlayerHasNoFourSoldiersXmlException();
            }
        }

    }

    public void parseBoardToXml(xmlPackage.Snakesandladders gameToSave) {
        xmlPackage.Board xmlBoard = new xmlPackage.Board();
        gameToSave.setBoard(xmlBoard);
        gameToSave.getBoard().setSize(m_board.getBoardSize());
        xmlPackage.Cells xmlCells = new xmlPackage.Cells();
        xmlBoard.setCells(xmlCells);
        xmlPackage.Snakes xmlSnakes = new xmlPackage.Snakes();
        xmlPackage.Ladders xmlLadders = new xmlPackage.Ladders();
        xmlBoard.setLadders(xmlLadders);
        xmlBoard.setSnakes(xmlSnakes);
        for (Cell c : m_board.getCells()) {
            if (c.isThereSoldiers()) {
                xmlPackage.Cell xmlCell = new xmlPackage.Cell();
                xmlCell.setNumber(BigInteger.valueOf(c.getCellNum()));

                int i;
                for (i = 0; i < c.getSoldiersInCell().length; i++) {
                    if (c.getSoldiersInCell()[i] != GameBoard.EMPTY) {
                        xmlPackage.Cell.Soldiers xmlCellSoldier = new xmlPackage.Cell.Soldiers();
                        xmlCellSoldier.setPlayerName(getPlayerByNum(i + 1).getPlayerName());
                        xmlCellSoldier.setCount(c.getSoldiersInCell()[i]);
                        xmlCell.getSoldiers().add(xmlCellSoldier);
                    }
                }
                xmlCells.getCell().add(xmlCell);
            }

            if (c.getDest() != Cell.NO_DEST) {
                if (c.getCellNum() < c.getDest()) {
                    xmlPackage.Ladders.Ladder xmlLadder = new xmlPackage.Ladders.Ladder();
                    xmlLadder.setFrom(BigInteger.valueOf(c.getCellNum()));
                    xmlLadder.setTo(BigInteger.valueOf(c.getDest()));
                    xmlLadders.getLadder().add(xmlLadder);
                } else {
                    xmlPackage.Snakes.Snake xmlSnake = new xmlPackage.Snakes.Snake();
                    xmlSnake.setFrom(BigInteger.valueOf(c.getCellNum()));
                    xmlSnake.setTo(BigInteger.valueOf(c.getDest()));
                    xmlSnakes.getSnake().add(xmlSnake);
                }
            }
        }
    }

    private void getCurrentPlayerFromXml(Snakesandladders gameXml, LinkedList<String> playerNames) throws XmlIsInvalidException {
        if (!playerNames.contains(gameXml.getCurrentPlayer())) {
            throw new CurrentPlayerIsNotInPlayerListXml();
        }
        currPlayer = playerList.get(playerNames.indexOf(gameXml.getCurrentPlayer()));
        playerItr = playerList.listIterator();
        setIteratorOnFirstPlayer(playerItr, currPlayer);
    }

    public Player getPlayerByNum(int playerNum) {
        int i;
        Player res = null;
        boolean isFound = false;
        for (i = 0; i < playerList.size() && !isFound; i++) {
            if (playerList.get(i).getPlayerNum() == playerNum) {
                isFound = true;
                res = playerList.get(i);
            }
        }
        return res;
    }

    private void checkIfAllXmlGamePlayersAreComp(xmlPackage.Snakesandladders gameXml) throws OnlyComputerPlayersXmlException {
        boolean allAreComp = true;

        for (xmlPackage.Players.Player p : gameXml.getPlayers().getPlayer()) {
            if (p.getType() == HUMAN) {
                allAreComp = false;
            }
        }

        if (allAreComp) {
            throw new OnlyComputerPlayersXmlException();
        }
    }

    public void checkIfXmlGameAlreadyFinished(int numSoldiersToWin, List<xmlPackage.Cell> cellsXml, int xmlBoardSize)
            throws XmlIsInvalidException {
        for (xmlPackage.Cell c : cellsXml) {
            if (c.getNumber().intValue() == xmlBoardSize * xmlBoardSize) {
                for (xmlPackage.Cell.Soldiers s : c.getSoldiers()) {
                    if (s.getCount() == numSoldiersToWin) {
                        throw new GameAlreadyFinishedXmlException();
                    }
                }
            }
        }
    }

    public xmlPackage.Players parsePlayersToXml(xmlPackage.Snakesandladders gameToSave) {
        xmlPackage.Players xmlPlayers = new xmlPackage.Players();
        for (Player p : playerList) {
            xmlPackage.Players.Player xmlPlayer = new xmlPackage.Players.Player();
            xmlPlayer.setName(p.getPlayerName());
            if (p.getType() == Player.PlayerType.COMP) {
                xmlPlayer.setType(xmlPackage.PlayerType.COMPUTER);
            } else {
                xmlPlayer.setType(xmlPackage.PlayerType.HUMAN);
            }
            xmlPlayers.getPlayer().add(xmlPlayer);

        }
        return xmlPlayers;
    }

    public boolean isXMLGame() {
        return gameSrc.equals(LoadedFrom.XML);
    }

    public boolean isGameStarted() {
        isStarted = getJoinedCount() == getM_numPlayers();
        return isStarted;
    }

    public Player getCurrPlayer() {
        return currPlayer;
    }

    public GameBoard getBoard() {
        return m_board;
    }

    public LinkedList<Player> getPlayerList() {
        return playerList;
    }

    public void removePlayerFromGame(Player player) {
        player.removePlayerSoldiersFromGame();
        // playerItr.remove();
        if (playerItr.equals(player)) {
            playerItr.remove();
        } else {
            Player p = this.getCurrPlayer();
            setIteraorOnPlayer(player);
            //playerItr.set(player);
            playerItr.remove();
            setIteraorOnPlayer(p);
        }
        //this.getPlayerList().remove(player);
    }

    private void setIteraorOnPlayer(Player p) {

        Player tmpPlayer = null;
        playerItr = playerList.listIterator();
        while (playerItr.hasNext() && p != tmpPlayer) {
            tmpPlayer = playerItr.next();
        }
    }

    public int getNumSoldiersToWin() {
        return m_numSoldiersToWin;
    }

    /**
     * @return the m_numPlayers
     */
    public int getM_numPlayers() {
        return m_numPlayers;
    }

    /**
     * @return the gameName
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * @return the joinedCount
     */
    public int getJoinedCount() {
        return joinedCount;
    }

    public void decrementJoinedCount() {
        joinedCount--;
    }

}
