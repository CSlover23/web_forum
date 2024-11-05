package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaDaoHibernateimpl")
public class AlphaDaoHibernateimpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate  ";
    }
}
