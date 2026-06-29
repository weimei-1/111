package com.supermarket.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.MemberLevel;
import com.supermarket.mapper.MemberLevelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Override
    public void run(String... args) {
        // 检查是否已有等级数据
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (memberLevelMapper.selectCount(wrapper) > 0) {
            return;
        }

        // 初始化默认会员等级
        List<MemberLevel> defaultLevels = Arrays.asList(
            createLevel("普通会员", 0, BigDecimal.ONE, BigDecimal.ONE, 1, 1),
            createLevel("银卡会员", 500, new BigDecimal("0.95"), new BigDecimal("1.2"), 2, 1),
            createLevel("金卡会员", 2000, new BigDecimal("0.90"), new BigDecimal("1.5"), 3, 1),
            createLevel("钻石会员", 5000, new BigDecimal("0.85"), new BigDecimal("2.0"), 4, 1)
        );

        for (MemberLevel level : defaultLevels) {
            memberLevelMapper.insert(level);
        }
    }

    private MemberLevel createLevel(String name, int minSpent, BigDecimal discountRate,
                                     BigDecimal pointsMultiplier, int sort, int status) {
        MemberLevel level = new MemberLevel();
        level.setName(name);
        level.setMinSpent(BigDecimal.valueOf(minSpent));
        level.setDiscountRate(discountRate);
        level.setPointsMultiplier(pointsMultiplier);
        level.setSort(sort);
        level.setStatus(status);
        return level;
    }
}