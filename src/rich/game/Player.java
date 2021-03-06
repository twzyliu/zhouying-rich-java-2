package rich.game;

import com.sun.tools.javac.util.Pair;
import rich.command.Command;
import rich.command.Response;
import rich.place.Land;
import rich.place.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    private Status status;
    private Command lastExecuted;
    private Place currentPlace;
    private Map<Tool, Integer> tools;
    private GameMap map;
    private int balance;
    private List<Land> lands;
    private int waitTimes;
    private boolean isInPrison;
    private boolean isBombIntoHospital;
    private int noPunishTimes;
    private int points;
    private String name;

    public Player() {
        this.status = Status.WAIT_FOR_COMMAND;
        this.lastExecuted = null;
        this.currentPlace = null;
        this.tools = new HashMap<>();
        this.balance = 0;
        this.waitTimes = 0;
        this.isInPrison = false;
        this.isBombIntoHospital = false;
        this.noPunishTimes = 0;
        this.points = 0;
    }

    public Player(GameMap map, int startMoney) {
        this.status = Status.WAIT_FOR_COMMAND;
        this.lastExecuted = null;
        this.currentPlace = map.getStarting();
        this.tools = new HashMap<>();
        this.map = map;
        this.balance = startMoney;
        this.lands = new ArrayList<>();
        this.waitTimes = 0;
        this.isInPrison = false;
        this.isBombIntoHospital = false;
        this.noPunishTimes = 0;
        this.points = 0;
        map.addPlayer(this);
    }

    public Player(GameMap map, String name, int startMoney) {
        this.name = name;
        this.status = Status.WAIT_FOR_COMMAND;
        this.lastExecuted = null;
        this.currentPlace = map.getStarting();
        this.tools = new HashMap<>();
        this.map = map;
        this.balance = startMoney;
        this.lands = new ArrayList<>();
        this.waitTimes = 0;
        this.isInPrison = false;
        this.isBombIntoHospital = false;
        this.noPunishTimes = 0;
        this.points = 0;
        map.addPlayer(this);
    }

    public Status getStatus() {
        return status;
    }

    public Pair<Status, Message> execute(Command command) {
        Pair<Status, Message> result = command.execute(this);
        lastExecuted = command;
        status = result.fst;
        return result;
    }

    public Pair<Status, Message> respond(Response response) {
        Pair<Status, Message> result = lastExecuted.respondWith(this, response);
        status = result.fst;
        return result;
    }

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public boolean buyTool(Tool tool) {
        if (points >= tool.points() && getToolQuantityAmount() < GameConstant.MAX_TOOL_QUANTITY) {
            points -= tool.points();
            int cur = tools.getOrDefault(tool, 0);
            tools.put(tool, cur + 1);
            return true;
        }
        return false;
    }

    public int getQuantityByKind(Tool tool) {
        return tools.getOrDefault(tool, 0);
    }

    public boolean buyLand(Land land) {
        int price = land.getPrice();
        if (balance >= price) {
            lands.add(land);
            balance -= price;
            land.setOwner(this);
            return true;
        }
        return false;
    }

    public boolean upgradeLand(Land land) {
        int price = land.getPrice();
        if (balance >= price) {
            land.upgrade();
            balance -= price;
            return true;
        }
        return false;
    }

    public int getBalance() {
        return balance;
    }

    public List<Land> getLands() {
        return lands;
    }

    public void moveTo(Place target) {
        this.currentPlace = target;
    }

    public boolean payPassFee(int passFee) {
        if (balance >= passFee) {
            balance -= passFee;
            return true;
        }
        return false;
    }

    public void gainPassFee(int passFee) {
        balance += passFee;
    }

    public void pauseByPrison() {
        this.waitTimes = GameConstant.DAYS_IN_PRISON;
        this.isInPrison = true;
    }

    public int getWaitTimes() {
        return waitTimes;
    }

    public boolean isInPrison() {
        return isInPrison;
    }

    public boolean isBombIntoHospital() {
        return isBombIntoHospital;
    }

    public boolean canBePunished() {
        return noPunishTimes <= 0;
    }

    public void pauseByBomb() {
        isBombIntoHospital = true;
        waitTimes = GameConstant.DAYS_BOMBED_INTO_HOSPITAL;
    }

    public void blessed() {
        noPunishTimes = GameConstant.DAYS_WITH_MASCOT;
    }

    public void gainPoints(int points) {
        this.points = points;
    }

    public int getCurrentPoints() {
        return points;
    }

    public int getToolQuantityAmount() {
        return getQuantityByKind(Tool.Block) + getQuantityByKind(Tool.Robot) + getQuantityByKind(Tool.Bomb);
    }

    public void gainBonus() {
        balance += GameConstant.BONUS_MONEY;
    }

    public void useBlock() {
        tools.put(Tool.Block, tools.get(Tool.Block) - 1);
    }

    public void useBomb() {
        tools.put(Tool.Bomb, tools.get(Tool.Bomb) - 1);
    }

    public void setWaitTimes(int waitTimes) {
        this.waitTimes = waitTimes;
    }

    public String getName() {
        return name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {WAIT_FOR_RESPONSE, END_TURN, GAME_OVER, WAIT_FOR_COMMAND}

    public String query() {
        int zeroLevelAmount = 0;
        int oneLevelAmount = 0;
        int twoLevelAmount = 0;
        int threeLevelAmount = 0;
        for (Land land : lands) {
            switch (land.getLevel()) {
                case 0:
                    zeroLevelAmount += 1;
                    break;
                case 1:
                    oneLevelAmount += 1;
                    break;
                case 2:
                    twoLevelAmount += 1;
                    break;
                case 3:
                    threeLevelAmount += 1;
                    break;
            }
        }
        return  "资金: $" + balance + "\n" +
                "点数: " + points + "\n" +
                "地产: " + "空地: " + zeroLevelAmount + "处; " +
                "茅屋: " + oneLevelAmount + "处; " +
                "洋房: " + twoLevelAmount + "处; " +
                "摩天楼: " + threeLevelAmount + "处\n" +
                "道具: " +
                "路障: " + tools.getOrDefault(Tool.Block, 0) + "个; " +
                "炸弹: " + tools.getOrDefault(Tool.Bomb, 0) + "个; " +
                "机器娃娃: " + tools.getOrDefault(Tool.Robot, 0) + "个\n\n";
    }
}
