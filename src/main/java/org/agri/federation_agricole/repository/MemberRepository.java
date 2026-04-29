package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.CreateMember;
import org.agri.federation_agricole.entity.Enum.Gender;
import org.agri.federation_agricole.entity.Enum.Occupation;
import org.agri.federation_agricole.entity.Member;
import org.agri.federation_agricole.entity.Structure;
import org.agri.federation_agricole.exception.UnAuthorizeException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MemberRepository {
    private DataSource dataSource;
    public MemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member createMember(CreateMember member) {
        String query = """
                insert into members(id, first_name, last_name, birth_date, gender, address, profession, phone, email)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?) returning *
                """;
        String attachQuery = "insert into collectivity_members values (?,?,?::occupation_type) returning *";
        String id = null;
        Member m = new Member();
        try (Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, getNextId(member));
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setDate(4, Date.valueOf(member.getBirthDate()));
            stmt.setString(5, member.getGender().toString());
            stmt.setString(6, member.getAddress());
            stmt.setString(7, member.getProfession());
            stmt.setString(8, member.getPhone());
            stmt.setString(9, member.getEmail());
            ResultSet resultSet= stmt.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getString("id");
               m.setId(resultSet.getString("id"));
               m.setFirstName(resultSet.getString("first_name"));
               m.setLastName(resultSet.getString("last_name"));
               m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
               m.setGender(Gender.valueOf(resultSet.getString("gender")));
               m.setAddress(resultSet.getString("address"));
               m.setEmail(resultSet.getString("email"));
               m.setPhone(resultSet.getString("phone"));
               m.setProfession(resultSet.getString("profession"));
               m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
            }
            PreparedStatement stmt2 = conn.prepareStatement(attachQuery);
            stmt2.setString(1, member.getCollectivityId());
            stmt2.setString(2, id);
            stmt2.setString(3, Occupation.JUNIOR.toString());
            ResultSet resultSet1 = stmt2.executeQuery();
            if (resultSet1.next()) {
                m.setOccupation(Occupation.valueOf(resultSet1.getString("occupation")));
            }
            conn.commit();
            return m;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Member> createMembers(List<CreateMember> members) {
        List<Member> memberList = new ArrayList<>();
        for (CreateMember member : members) {
            memberList.add(createMember(member));
        }
        return memberList;
    }

    public Member getMemberById(String id){
        String query = """
                select
                 id, last_name, first_name, birth_date, gender, address, profession, phone, email,
                 registration_date
                from member where id=?
                """;
        Member member = new Member();
        try(Connection conn = dataSource.getConnection()){
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                member.setId(rs.getString("id"));
                member.setLastName(rs.getString("last_name"));
                member.setFirstName(rs.getString("first_name"));
                member.setBirthDate(rs.getDate("birth_date").toLocalDate());
                member.setGender(Gender.valueOf(rs.getString("gender")));
                member.setAddress(rs.getString("address"));
                member.setProfession(rs.getString("profession"));
                member.setPhone(rs.getString("phone"));
                member.setEmail(rs.getString("email"));
                member.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
            }
            return member;
        }catch(SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Member> getCollectivityMemberById(String collectivityId){
        String query = """
                SELECT m.id, m.last_name, m.first_name, m.birth_date, m.gender,
                     m.address, m.profession, m.phone, m.email, m.registration_date
                FROM members m
                JOIN collectivity_members cm ON cm.member_id = m.id
                WHERE cm.collectivity_id = ?
        """;
        List<Member> collectivityMembers = new ArrayList<>();
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Member m = new Member();
                m.setId(resultSet.getString("id"));
                m.setLastName(resultSet.getString("last_name"));
                m.setFirstName(resultSet.getString("first_name"));
                m.setGender(Gender.valueOf(resultSet.getString("gender")));
                m.setAddress(resultSet.getString("address"));
                m.setProfession(resultSet.getString("profession"));
                m.setPhone(resultSet.getString("phone"));
                m.setEmail(resultSet.getString("email"));
                m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
                collectivityMembers.add(m);
            }
            return collectivityMembers;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public Structure getStructureByCollectivityId(String Id){
        String query = """
                SELECT m.id, m.last_name, m.first_name, m.birth_date, m.gender,
                    m.address, m.profession, m.phone, m.email, m.registration_date
                FROM members m
                JOIN collectivity_members cm ON cm.member_id = m.id
                WHERE cm.collectivity_id = ?
                """;
        Structure structure = new Structure();

        try (Connection conn = dataSource.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, Id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Member m = new Member();
                if (Occupation.valueOf(resultSet.getString("occupation"))== Occupation.PRESIDENT){
                    m.setId(resultSet.getString("id"));
                    m.setLastName(resultSet.getString("last_name"));
                    m.setFirstName(resultSet.getString("first_name"));
                    m.setGender(Gender.valueOf(resultSet.getString("gender")));
                    m.setAddress(resultSet.getString("address"));
                    m.setProfession(resultSet.getString("profession"));
                    m.setPhone(resultSet.getString("phone"));
                    m.setEmail(resultSet.getString("email"));
                    m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                    m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
                    m.setOccupation(Occupation.valueOf(resultSet.getString("occupation")));
                    structure.setPRESIDENT(m);
                }
                if (Occupation.valueOf(resultSet.getString("occupation"))== Occupation.VICE_PRESIDENT){
                    m.setId(resultSet.getString("id"));
                    m.setLastName(resultSet.getString("last_name"));
                    m.setFirstName(resultSet.getString("first_name"));
                    m.setGender(Gender.valueOf(resultSet.getString("gender")));
                    m.setAddress(resultSet.getString("address"));
                    m.setProfession(resultSet.getString("profession"));
                    m.setPhone(resultSet.getString("phone"));
                    m.setEmail(resultSet.getString("email"));
                    m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                    m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
                    m.setOccupation(Occupation.valueOf(resultSet.getString("occupation")));
                    structure.setVICE_PRESIDENT(m);
                }
                if (Occupation.valueOf(resultSet.getString("occupation"))== Occupation.TREASURER){
                    m.setId(resultSet.getString("id"));
                    m.setLastName(resultSet.getString("last_name"));
                    m.setFirstName(resultSet.getString("first_name"));
                    m.setGender(Gender.valueOf(resultSet.getString("gender")));
                    m.setAddress(resultSet.getString("address"));
                    m.setProfession(resultSet.getString("profession"));
                    m.setPhone(resultSet.getString("phone"));
                    m.setEmail(resultSet.getString("email"));
                    m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                    m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
                    m.setOccupation(Occupation.valueOf(resultSet.getString("occupation")));
                    structure.setTREASURER(m);
                }
                if (Occupation.valueOf(resultSet.getString("occupation"))== Occupation.SECRETARY){
                    m.setId(resultSet.getString("id"));
                    m.setLastName(resultSet.getString("last_name"));
                    m.setFirstName(resultSet.getString("first_name"));
                    m.setGender(Gender.valueOf(resultSet.getString("gender")));
                    m.setAddress(resultSet.getString("address"));
                    m.setProfession(resultSet.getString("profession"));
                    m.setPhone(resultSet.getString("phone"));
                    m.setEmail(resultSet.getString("email"));
                    m.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                    m.setRegistrationDate(resultSet.getDate("registration_date").toLocalDate());
                    m.setOccupation(Occupation.valueOf(resultSet.getString("occupation")));
                    structure.setSECRETARY(m);
                }
            }
            return structure;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getLastId(CreateMember createMember) {
        String lastId = null;
        String ilikeVar = createMember.getCollectivityId();
        String[] ilikeVarArray = ilikeVar.split("-");
        ilikeVar = ilikeVarArray[0];
        String query = "SELECT id FROM members where id ilike ? order by id desc limit 1";
        try (Connection conn = dataSource.getConnection()){
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,"'%"+ilikeVar+"%'");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                lastId = resultSet.getString("id");
            }
            else {
                lastId = ilikeVar+"-M0";
            }
            return lastId;
        }catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    String getNextId(CreateMember createMember) {
        String lastId = getLastId(createMember);
        String[] parts = lastId.split("-");
        if(parts.length != 2){
            throw new UnAuthorizeException("Invalid last id format");
        }
        int id = Integer.parseInt(parts[1].substring(1));
        id += 1;
        return parts[0]+"-M"+id;
    }

}
