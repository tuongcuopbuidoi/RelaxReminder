package com.example.dell.relaxreminder.Model;

/**
 * Created by DELL on 3/17/2019.
 */

public class DateLog {

    private Long ID;
    private  int relaxNeed;
    private int relaxDone;
    private String date ;


    public String getDate(){return date;}

    public  void setDate(String date) {
        this.date = date;
    }

    public int getRelaxDone() {return relaxDone;}

    public void setRelaxDone(int relaxDone) {
        this.relaxDone = relaxDone;
    }

    public int getRelaxNeed() {return relaxNeed;}

    public Double getRelaxInLiter(int relax){
        double w= (double) relax;
        return w/1000.0;
    }
    public int getRelaxInMLiter(int relax){
        return relax*1000;
    }
    public void setRelaxNeed(int relaxNeed) {
        this.relaxNeed = relaxNeed;
    }



    public void setID(Long ID) {
        this.ID =  ID;
    }
    public Long getID() {
        return ID;
    }
}
