package cn.ideabuffer.async.test.bean;

import java.io.*;

/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
public class User implements Serializable {

    private String name;

    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        System.out.println("in getName");
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            ", age=" + age +
            '}';
    }

}
