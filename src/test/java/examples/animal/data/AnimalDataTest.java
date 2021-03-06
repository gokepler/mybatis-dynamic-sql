/**
 *    Copyright 2016-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteSupport;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;

@RunWith(JUnitPlatform.class)
public class AnimalDataTest {
    
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(AnimalDataMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectAllRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(animals.size()).isEqualTo(65);
                softly.assertThat(animals.get(0).getId()).isEqualTo(1);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectAllRowsWithOrder() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(animals.size()).isEqualTo(65);
                softly.assertThat(animals.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testSelectRowsLessThan20() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(19);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsBetween30And40() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(30).and(40))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(11);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsNotBetweenWithProvider() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testSelectRowsNotBetween() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(5))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(5))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(64);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsGreaterThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(6);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsLessThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(10);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(3);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInCaseSensitiveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testNotInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(62);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotInCaseSensitiveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCaseInsensitive() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testNotLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotLikeCaseInsensistveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDeleteThreeRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteFrom(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rowCount = mapper.delete(deleteSupport);
            assertThat(rowCount).isEqualTo(3);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testComplexDelete() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteFrom(animalData)
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rowCount = mapper.delete(deleteSupport);
            assertThat(rowCount).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(0);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(65);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testComplexCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .orderBy(id.descending(), bodyWeight)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(4);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testUpdateByExample() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setBodyWeight(2.6);
            
            UpdateSupport updateSupport = update(animalData)
                    .set(bodyWeight).equalTo(record.getBodyWeight())
                    .set(animalName).equalToNull()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rows = mapper.update(updateSupport);
            assertThat(rows).isEqualTo(4);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertSupport<AnimalData> insertSupport = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toProperty("animalName")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            int rows = mapper.insert(insertSupport);
            assertThat(rows).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInsertNull() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertSupport<AnimalData> insertSupport = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            int rows = mapper.insert(insertSupport);
            assertThat(rows).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testOrderByAndDistinct() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending(), animalName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows.size()).isEqualTo(14);
                softly.assertThat(rows.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testOrderByWithFullClause() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows.size()).isEqualTo(14);
                softly.assertThat(rows.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testCount() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(count())
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("count(*)");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select count(*) from AnimalData a");
            
                Long count = mapper.selectALong(selectSupport);
                softly.assertThat(count).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testCountNoAlias() {
        SqlTable animalDataNoAlias = SqlTable.of("AnimalData");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(count())
                    .from(animalDataNoAlias)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            Long count = mapper.selectALong(selectSupport);
            assertThat(count).isEqualTo(65);
        } finally {
            sqlSession.close();
        }
    }
}
