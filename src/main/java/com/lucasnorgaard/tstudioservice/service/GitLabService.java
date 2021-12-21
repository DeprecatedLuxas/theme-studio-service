package com.lucasnorgaard.tstudioservice.service;


import org.springframework.stereotype.Service;
@Service
public class GitLabService {

    private final Integer MAX_AMOUNT = 20;


//    public GHRepository getRepoWithId(String id) {
//        GHRepository repo;
//        try {
//            repo = Application.getGitHub().getRepositoryById(Long.parseLong(id));
//        } catch (IOException e) {
//            throw new RepositoryNotFoundException(id);
//        }
//        if (repo == null) {
//            throw new RepositoryNotFoundException(id);
//        }
//        return repo;
//    }
//
//    public List<Content> getContent(GHRepository repository) {
//        List<Content> contents = new ArrayList<>();
//        try {
//            List<GHContent> rootContent = repository.getDirectoryContent("/");
//            int index = 0;
//            for (GHContent content : rootContent) {
//
////                if (content.isDirectory()) {
////                    Content contentObj = new Content(
////                            content.getName(),
////                            "dir",
////                            content.getPath(),
////                            "",
////                            ""
////                    );
////
////                }
////                if (content.isFile()) {
////                    Optional<String> extension = Utils.getFileExtension(content.getName());
////                    if (extension.isPresent()) {
////                        Content contentObj = new Content(
////                                content.getName(),
////                                extension.get(),
////                                "",
////                                "",
////                                ""
////                        );
////                        contents.add(contentObj);
////                    }
////                }
////                if (content.isDirectory())
////                System.out.println(content.listDirectoryContent());
//                index++;
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return contents;
//    }
}
