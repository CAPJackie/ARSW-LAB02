package edu.eci.arsw.highlandersim;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    
    private boolean isLocked = false;
    
    private ControlFrame locker;
    private boolean isDead = false;
    private boolean isFinished = false;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {
        if(immortalsPopulation.size()==1){
            finish();
        }
        while (!isDead && !isFinished) {
            if(!isLocked){
                
                if(health == 0){
                    isDead = true;
                    immortalsPopulation.remove(this);
                    break;
                }
                
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex ) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }
                

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                synchronized(locker){
                    try {
                        locker.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    public void fight(Immortal i2) {
        if(this.hashCode()<i2.hashCode()){
            synchronized(this){
                synchronized(i2){
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                    } else {
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                        
                    }
                }
            }
        }else{
            synchronized(i2){
                synchronized(this){
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                    } else {
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    }
                }
            }
        }

    }

    public boolean isIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isIsDead() {
        return isDead;
    }

    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    
    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    public ControlFrame getLocker() {
        return locker;
    }

    public void setLocker(ControlFrame locker) {
        this.locker = locker;
    }
    
    public void finish(){
        isFinished = true;
    }
    
    public void lock(){
        isLocked = true;
    }

    public void unlock(){
        synchronized(locker){
            locker.notify();
        }
        isLocked = false;
    }

    public boolean isIsFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
    
    

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    


    

}
