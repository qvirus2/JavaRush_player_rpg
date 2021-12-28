package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.PlayerNotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl {
    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void save(Player player) {
        playerRepository.save(player);
    }

    public List<Player> getAllExistingPlayersList(Specification<Player> specification) {
        return playerRepository.findAll(specification);
    }
    public Page<Player> getAllExistingPlayersList(Specification<Player>  specification, Pageable sortedByName) {
        return playerRepository.findAll(specification, sortedByName);
    }


    public Specification<Player> nameFilter(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
    public Specification<Player> titleFilter(String title) {
        return (root, query, criteriaBuilder) -> title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public Specification<Player> professionFilter(Profession profession) {
        return (root, query, criteriaBuilder) -> profession == null ? null : criteriaBuilder.equal(root.get("profession"), profession);
    }
    public Specification<Player> raceFilter(Race race) {
        return (root, query, criteriaBuilder) -> race == null ? null : criteriaBuilder.equal(root.get("race"), race);
    }

    public Specification<Player> bannedFilter(Boolean isBanned) {
        return (root, query, criteriaBuilder) -> {
            if (isBanned == null) {
                return null;
            }
            if (isBanned) {
                return criteriaBuilder.isTrue(root.get("banned"));
            } else {
                return criteriaBuilder.isFalse(root.get("banned"));
            }
        };
    }
    public Specification<Player> levelFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), min);
            }
            return criteriaBuilder.between(root.get("level"), min, max);
        };
    }
    public Specification<Player> experienceFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), min);
            }
            return criteriaBuilder.between(root.get("experience"), min, max);
        };
    }


    public Specification<Player> dateFilter(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date before1 = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), before1);
            }
            if (before == null) {
                Date after1 = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), after1);
            }
            //time difference
            Date before1 = new Date(before - 3600001);
            Date after1 = new Date(after);
            return criteriaBuilder.between(root.get("birthday"), after1, before1);
        };
    }


    public Integer idChecker(String id) {
        if (id == null || id.equals("0") || id.equals("")) {
            throw new BadRequestException("ID is incorrect");
        }
        try {
            Integer iD = Integer.parseInt(id);
            return iD;
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID is not a number", e);
        }
    }
    private void paramsChecker(Player playerRequired) {

        if (playerRequired.getName() != null && (playerRequired.getName().length() < 1 || playerRequired.getName().length() > 12)) {
            throw new BadRequestException("The player name is too long or absent");
        }

        if (playerRequired.getTitle() != null && playerRequired.getTitle().length() > 30) {
            throw new BadRequestException("The title is too long or absent");
        }
/**/
        if (playerRequired.getExperience() != null && (playerRequired.getExperience() < 0 || playerRequired.getExperience() > 10000000)) {
            throw new BadRequestException("The Experience size is out of range");
        }

       if (playerRequired.getBirthday() != null) {
            Calendar date = Calendar.getInstance();
            date.setTime(playerRequired.getBirthday());
            if (date.get(Calendar.YEAR) < 2000 || date.get(Calendar.YEAR) > 3000) {
                throw new BadRequestException("The date of player Birthday is out of range");
            }
        }
    }
    private Integer calculateLevel(Player playerRequired) {
        BigDecimal level = new BigDecimal((Math.sqrt(2500+200*playerRequired.getExperience())-50) / 100);
        return level.intValue();
    }

    private Integer calculateuntilNextLevel(Player playerRequired) {
        BigDecimal untilNextLevel = new BigDecimal(50 * (playerRequired.getLevel() + 1) * (playerRequired.getLevel()  + 2) - playerRequired.getExperience());
        return untilNextLevel.intValue();
    }





    //////////////////////////////////////////////////////////////////////////////////////////////////
    //возвращает лист всех сущностей из базы
    public Page<Player> getAll(Pageable paging) {
        return playerRepository.findAll(paging);
    }

    @Transactional
    public Optional<Player> getById(int id) {
        if (!playerRepository.existsById(id)) {
            throw new PlayerNotFoundException("Player is not found");
        }else {
            return playerRepository.findById(id);
        }
    }

    @Transactional
    public void deleteByID (int id) {
        if (!playerRepository.existsById(id)) {
            throw new PlayerNotFoundException("Player is not found");
        }else {
            playerRepository.deleteById(id);
        }

    }
    @Transactional
    public Player createPlayer(Player playerRequired) {
        if (playerRequired.getName() == null
                || playerRequired.getTitle() == null
                || playerRequired.getRace() == null
                || playerRequired.getBirthday() == null
                || playerRequired.getProfession() == null
                || playerRequired.getExperience() == null)
                {
            throw new BadRequestException("Please fill in all required fields");
        }
        paramsChecker(playerRequired);
        if (playerRequired.isBanned() == null) {
            playerRequired.setBanned(false);
        }
        Integer level = calculateLevel(playerRequired);
        playerRequired.setLevel(level);
        Integer untilNextLevel =  calculateuntilNextLevel(playerRequired);
        playerRequired.setUntilNextLevel(untilNextLevel);
        return playerRepository.save(playerRequired);
    }



    @Transactional
    public Player editPlayer(Integer id, Player playerRequired) {
        paramsChecker(playerRequired);
        if (!playerRepository.existsById(id))
            throw new PlayerNotFoundException("Player is not found");

        Player changedPlayer = playerRepository.findById(id).get();

        if (playerRequired.getName() != null)
            changedPlayer.setName(playerRequired.getName());

        if (playerRequired.getBirthday() != null)
            changedPlayer.setBirthday(playerRequired.getBirthday());

        if (playerRequired.getLevel() != null)
            changedPlayer.setLevel(playerRequired.getLevel());

        if (playerRequired.getTitle() != null)
            changedPlayer.setTitle(playerRequired.getTitle());

        if (playerRequired.isBanned() != null)
            changedPlayer.setBanned(playerRequired.isBanned());

        if (playerRequired.getExperience() != null)
            changedPlayer.setExperience(playerRequired.getExperience());

        if (playerRequired.getProfession() != null)
            changedPlayer.setProfession(playerRequired.getProfession());
        if (playerRequired.getRace() != null)
            changedPlayer.setRace(playerRequired.getRace());

        Integer level = calculateLevel(changedPlayer);
        changedPlayer.setLevel(level);
       Integer untilNextLevel =  calculateuntilNextLevel(changedPlayer);
            changedPlayer.setUntilNextLevel(untilNextLevel);
        return playerRepository.save(changedPlayer);
    }








    }
