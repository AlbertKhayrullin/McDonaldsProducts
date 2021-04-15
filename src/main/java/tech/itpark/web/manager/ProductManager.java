package tech.itpark.web.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.itpark.web.dto.ProductDto;
import tech.itpark.web.dto.ProductUpdateDto;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductManager {
    private final DataSource dataSource;
    public List<ProductDto> getAll() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT id, name, price, qty FROM products ORDER BY id");
        ) {
            List<ProductDto> result = new LinkedList<>();
            while (resultSet.next()) {
                long id = resultSet.getLong("id" );
                String name = resultSet.getString("name" );
                int price = resultSet.getInt("price" );
                int qty = resultSet.getInt("qty" );
                result.add(new ProductDto(id, name, price, qty));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductUpdateDto save (ProductUpdateDto dto) {
        if (dto.getId() == 0) {
            return create(dto);
        }
        return update(dto);
    }

    public ProductUpdateDto create (ProductUpdateDto dto) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO products ( name, price, qty )
                    VALUES ( ?, ?, ? );
                    """, Statement.RETURN_GENERATED_KEYS);
        )   {
            int index = 0;
            statement.setString(++index, dto.getName());
            statement.setInt(++index, dto.getPrice());
            statement.setInt(++index, dto.getQty());
            statement.executeUpdate();
            try (
                  ResultSet generatedKeys = statement.getGeneratedKeys();
            )  {
                if (generatedKeys.next()) {
                    dto.setId(generatedKeys.getLong(1));
                    return dto;
                }
                throw new RuntimeException("Keys not generated");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductUpdateDto update(ProductUpdateDto dto) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                UPDATE products
                SET name = ?, price = ?, qty = ?
                WHERE id = ?;
                """);
        ) {
           int index = 0;
           statement.setString(++index, dto.getName());
           statement.setInt(++index, dto.getPrice());
           statement.setInt(++index, dto.getQty());
           statement.setLong(++index, dto.getId());
           statement.executeUpdate();
           return dto;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
