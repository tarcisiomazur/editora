package dw.editora.control;

import dw.editora.model.Artigo;
import dw.editora.repository.ArtigoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ArtigoController {

    @Autowired
    private ArtigoRepository rep;

    @GetMapping("/")
    public ModelAndView index(Model model) {
        model.addAttribute("artigos", rep.findAll());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/artigos/novo")
    public ModelAndView showNovoArtigo(Model model) {
        model.addAttribute("artigo", new Artigo());
        return new ModelAndView("novoArtigo");
    }

    @GetMapping("/artigos/editar/{id}")
    public ModelAndView showEditarArtigo(@PathVariable("id") long id, Model model) {
        Optional<Artigo> artigo = rep.findById(id);
        if(artigo.isPresent()){
            model.addAttribute("artigo", artigo.get());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("updateArtigo");
            return modelAndView;
        }
        return new ModelAndView("redirect:/");
    }

    /*
    * GET /api/artigos : listar todos os artigos
    */
    @GetMapping("/artigos")
    public  ResponseEntity<List<Artigo>> getAllArtigos(@RequestParam(required = false) String titulo)
    {
        try
        {
            List<Artigo> la = new ArrayList<Artigo>();

            if (titulo == null)
                rep.findAll().forEach(la::add);
            else
                rep.findByTituloContaining(titulo).forEach(la::add);

            if (la.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            return new ResponseEntity<>(la, HttpStatus.OK);


        }
         catch (Exception e) {
            //TODO: handle exception
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

     /*
    * POST /api/artigos : criar artigo
    */
    @PostMapping("/artigos")
    public ModelAndView createArtigo(@Validated Artigo artigo)
    {
        try {
            rep.save(new Artigo(artigo.getTitulo(), artigo.getResumo(), artigo.isPublicado()));
        } catch (Exception e) {
            //TODO: handle exception
        }
        return new ModelAndView("redirect:/");
    }

     /*
    * GET /api/artigos/:id : listar artigo dado um id
    */
    @GetMapping("/artigos/{id}")
    public ResponseEntity<Artigo> getArtigoById(@PathVariable("id") long id)
    {
        Optional<Artigo> data = rep.findById(id);

        if (data.isPresent())
            return new ResponseEntity<>(data.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

     /*
    * POST /editora/artigos/update/:id : atualizar artigo dado um id
    */
    @PostMapping("/artigos/update/{id}")
    public ModelAndView updateArtigo(@PathVariable("id") long id, @Validated Artigo artigo, BindingResult result, Model model)
    {
        Optional<Artigo> data = rep.findById(id);

        if (data.isPresent() && !result.hasErrors())
        {
            Artigo ar = data.get();
            ar.setPublicado(artigo.isPublicado());
            ar.setResumo(artigo.getResumo());
            ar.setTitulo(artigo.getTitulo());
            try{
                rep.save(ar);
            }catch(Exception e) {
                //TODO: handle exception
            }
        }
        return new ModelAndView("redirect:/");

    }

     /*
    * POST /editora/delete/artigos/:id : remover artigo dado um id
    */
    @PostMapping("/artigos/delete/{id}")
    public ModelAndView deleteArtigo(@PathVariable("id") long id)
    {
        try {
            rep.deleteById(id);
        } catch (Exception e) {
            //TODO: handle exception
        }
        return new ModelAndView("redirect:/");
    }

     /*
    * DEL /editora/artigos : remover todos os artigos
    */
    @DeleteMapping("/artigos")
    public ResponseEntity<HttpStatus> deleteAllArtigo()
    {
        try {
            rep.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            //TODO: handle exception
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/oauthinfo")
    public String oauthUserInfo(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                                @AuthenticationPrincipal OAuth2User oauth2User) {

        return "User Name: " + oauth2User.getName() + "<br/>" +
                "User Authorities: " + oauth2User.getAuthorities() + "<br/>" +
                "Client Name: " + authorizedClient.getClientRegistration().getClientName() + "<br/>" +
                this.prettyPrintAttributes(oauth2User.getAttributes());
    }

    private String prettyPrintAttributes(Map<String, Object> attributes) {
        String acc = "User Attributes: <br/><div style='padding-left:20px'>";
        for (String key : attributes.keySet()) {
            Object value = attributes.get(key);
            acc += "<div>" + key + ":&nbsp" + value.toString() + "</div>";
        }
        return acc + "</div>";
    }


}
