package com.game.controller;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.game.service.PlayerServiceImpl;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest")
public class PlayerController {

    private PlayerServiceImpl playerService;

    @Autowired
    public void setPlayerService(PlayerServiceImpl playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(value = "/players", method = RequestMethod.GET)
    public List<Player> getAll(@RequestParam(defaultValue = "0") int pageNumber,
                               @RequestParam(defaultValue = "3") int pageSize,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "maxLevel", required = false) Double maxLevel,
                               @RequestParam(value = "minLevel", required = false) Double minLevel,
                               @RequestParam(value = "banned", required = false) Boolean banned,
                               @RequestParam(value = "after", required = false) Long after,
                               @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "profession", required = false) Profession profession,
                               @RequestParam(value = "race", required = false) Race race,
                               @RequestParam(value = "minExperience", required = false) Double minExperience,
                               @RequestParam(value = "maxExperience", required = false) Double maxExperience) {

        Pageable paging = PageRequest.of(pageNumber, pageSize);
        //if (name != null){
        Specification<Player> spec = Specification.where(
                        playerService.nameFilter(name))
                .and(playerService.titleFilter(title))
                .and(playerService.bannedFilter(banned))
                .and(playerService.levelFilter(minLevel, maxLevel))
                .and(playerService.dateFilter(after, before))
                .and(playerService.professionFilter(profession))
                .and(playerService.raceFilter(race))
                .and(playerService.experienceFilter(minExperience, maxExperience));
        return playerService.getAllExistingPlayersList(spec, paging).getContent();
        //} else {

        //    return playerService.getAll(paging).getContent();
        // }

    }

    @RequestMapping(value = "/players/count", method = RequestMethod.GET)
    @ResponseBody
    public long countEntities(@RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "title", required = false) String title,
                              @RequestParam(value = "maxLevel", required = false) Double maxLevel,
                              @RequestParam(value = "minLevel", required = false) Double minLevel,
                              @RequestParam(value = "banned", required = false) Boolean banned,
                              @RequestParam(value = "after", required = false) Long after,
                              @RequestParam(value = "before", required = false) Long before,
                              @RequestParam(value = "profession", required = false) Profession profession,
                              @RequestParam(value = "race", required = false) Race race,
                              @RequestParam(value = "minExperience", required = false) Double minExperience,
                              @RequestParam(value = "maxExperience", required = false) Double maxExperience) {

        Specification<Player> spec = Specification.where(
                        playerService.nameFilter(name))
                .and(playerService.titleFilter(title))
                .and(playerService.bannedFilter(banned))
                .and(playerService.levelFilter(minLevel, maxLevel))
                .and(playerService.dateFilter(after, before))
                .and(playerService.professionFilter(profession))
                .and(playerService.raceFilter(race))
                .and(playerService.experienceFilter(minExperience, maxExperience));
        return playerService.getAllExistingPlayersList(spec).size();
    }
    @RequestMapping(value = "/players/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Player editShip(@PathVariable("id") String id) {
        Integer iD = playerService.idChecker(id);
        return playerService.getById(iD).get();
    }
    @PostMapping("/players")
    public Player addShip(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }


    @RequestMapping(value = "/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Player editShip(@PathVariable("id") String id, @RequestBody Player payer) {
        Integer iD = playerService.idChecker(id);
        return playerService.editPlayer(iD, payer);
    }

/*    @RequestMapping(value = "/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Player createPlayer(@RequestBody Player player) {
        Optional<Player> playerResponse = playerService.getById(id);
        Player eplayer = playerResponse.get();
        eplayer.setName(player.getName());
        Player studentResponse = playerService.updatePlayer(eplayer);
        return studentResponse;
    }
*/




    @DeleteMapping("/players/{id}")
    public void deleteShip(@PathVariable("id") String id) {
        Integer iD = playerService.idChecker(id);
        playerService.deleteByID(iD);
    }
}
