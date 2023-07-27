package data.service;

import data.entity.AcademyInfoEntity;
import data.repository.AcademyInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import data.dto.AcademyBoardDto;
import data.entity.AcademyBoardEntity;
import data.entity.AcademylikeEntity;
import data.entity.MemberEntity;
import data.mapper.AcademyBoardMapper;
import data.repository.AcademyBoardRepository;
import data.repository.AcademylikeRepository;
import data.repository.MemberRepository;
import jwt.setting.settings.JwtService;
import lombok.extern.slf4j.Slf4j;
import naver.cloud.NcpObjectStorageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;





@Service
@Slf4j
public class AcademyBoardService {
    private final MemberRepository memberRepository;    

    private final AcademyBoardRepository academyBoardRepository;

    private final AcademylikeRepository academylikeRepository;

    private final NcpObjectStorageService storageService;

    private final AcademyBoardMapper academyBoardMapper;

    private final AcademyInfoRepository academyInfoRepository;

    private final JwtService jwtService;

    public AcademyBoardService(AcademyBoardRepository academyBoardRepository,  MemberRepository memberRepository, NcpObjectStorageService storageService, AcademylikeRepository academylikeRepository,AcademyBoardMapper academyBoardMapper,JwtService jwtService,
                               AcademyInfoRepository academyInfoRepository ) {
        this.academyBoardRepository = academyBoardRepository;
        this.academylikeRepository = academylikeRepository;
        this.memberRepository = memberRepository;
        this.storageService = storageService;
        this.academyBoardMapper = academyBoardMapper;
        this.jwtService = jwtService;
        this.academyInfoRepository = academyInfoRepository;

    }
    
    @Value("${aws.s3.bucketName}")
    private String bucketName;


    public AcademyBoardDto insertAcademyBoard(AcademyBoardDto dto, HttpSession session,HttpServletRequest request){
        try {
            int m_idx = jwtService.extractIdx(jwtService.extractAccessToken(request).get()).get();
            int AIidx = memberRepository.findById(m_idx).get().getAIidx();


            if(session.getAttribute("photo")!=null){
                dto.setAb_photo(session.getAttribute("photo").toString());
            }

            dto.setAi_idx(AIidx);
            AcademyBoardEntity academyBoard = AcademyBoardEntity.toAcademyBoardEntity(dto);
            academyBoardRepository.save(academyBoard);
            session.removeAttribute("photo");
            return dto;
        } catch (Exception e){
            log.error("insert AcademyBoard Error",e);
            throw  e;
        }
    }

    public List<String> uploadPhoto(List<MultipartFile> upload, HttpSession session){
        List<String> fullPhoto = new ArrayList<>();

        for(MultipartFile photo : upload ) {
            fullPhoto.add(storageService.uploadFile(bucketName,"devster/aboard",photo));
        }

        if(session.getAttribute("photo") != null) {
            storageService.deleteFile(bucketName,"devster/aboard",session.getAttribute("photo").toString());
        }

        session.setAttribute("photo",String.join(",",fullPhoto));
        log.info("AcademyBoard 사진 업로드 완료");
        return fullPhoto;
    }

    public void resetPhoto(String photo) {
        storageService.deleteFile(bucketName,"devster/aboard",photo);
        log.info("Academy 사진 초기화 완료");
    }




    // public AcademyBoardDto insertAcademyBoard(AcademyBoardDto dto,List<MultipartFile> upload){  
    //     String ab_photo="";
    //     if(upload.get(0).getOriginalFilename().equals("")){
    //         ab_photo="no";
    //     } else{
    //         int i=0;
    //         for(MultipartFile mfile : upload) {
    //             ab_photo += (storageService.uploadFile(bucketName, "devster/hireboard", mfile) + ",");
    //         }
    //     }
    //     ab_photo=ab_photo.substring(0,ab_photo.length()-1);
    //     dto.setAb_photo(ab_photo);

    //     try {
    //         AcademyBoardEntity entity = AcademyBoardEntity.toAcademyBoardEntity(dto);
    //         academyBoardRepository.save(entity);

    //         return dto;
    //     } catch (Exception e) {
    //         log.error("Error occurred while inserting hireboard",e);
    //         throw e;
    //     }
    // }


    // public List<AcademyBoardDto> getAllData(){
    //     try{
    //         List<AcademyBoardDto> list = new ArrayList<>();
    //         for(AcademyBoardEntity entity : academyBoardRepository.findAll()){
    //             list.add(AcademyBoardDto.toAcademyBoardDto(entity));
    //         }
    //         return list;
    //     } catch(Exception e){
    //         log.error("Error occurred while getting all hireboard data", e);
    //         throw e;
    //     }
    // }    

    public Map<String, Object> getPagedAcademyboard(int page, int size, String keyword, HttpServletRequest request, String sortProperty, String sortDirection) {
        int m_idx = jwtService.extractIdx(jwtService.extractAccessToken(request).get()).get();
        int AIidx = memberRepository.findById(m_idx).get().getAIidx();

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        Page<AcademyBoardEntity> result;

        if(keyword!=null && !keyword.trim().isEmpty()){
            result = academyBoardRepository.findByABsubjectContainingAndAIidx(keyword, AIidx, pageable);
        } else {
            result = academyBoardRepository.findByAIidx(AIidx, pageable);
        }
        
        // Pageable pageable = PageRequest.of(page, size, Sort.by("ABwriteday").descending());
        // Page<AcademyBoardEntity> result = academyBoardRepository.findByAIidx(AIidx, pageable);
        // Page<AcademyBoardEntity> result = academyBoardRepository.findAll(pageable);

        List<Map<String, Object>> academyBoardList = result
                .getContent()
                .stream()
                .map(academyBoardEntity -> {
                    MemberEntity memberInfo = memberRepository.findById(academyBoardEntity.getMIdx()).orElse(null);
                    AcademyInfoEntity academyInfo= academyInfoRepository.findById(academyBoardEntity.getAIidx()).orElse(null);
                    Map<String, Object> academyboardMemberInfo = new HashMap<>();
                    academyboardMemberInfo.put("aboard", AcademyBoardDto.toAcademyBoardDto(academyBoardEntity));

                    if (memberInfo != null) {
                        academyboardMemberInfo.put("mPhoto", memberInfo.getMPhoto());
                        academyboardMemberInfo.put("mNicname", memberInfo.getMNickname());
                    }

                    if(academyInfo != null){
                        academyboardMemberInfo.put("AIidx",academyInfo.getAIidx());
                        academyboardMemberInfo.put("AIname",academyInfo.getAIname());

                    }
                    return academyboardMemberInfo;
                })
                .collect(Collectors.toList());


        Map<String, Object> response = new HashMap<>();
        response.put("academyBoardList", academyBoardList);
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", result.getNumber() + 1);
        response.put("hasNext", result.hasNext());

        return response;
    }




    public AcademyBoardDto findByAbIdx(int idx){
        try {
            AcademyBoardEntity entity = academyBoardRepository.findById((Integer)idx)
                .orElseThrow(() -> new EntityNotFoundException("해당 idx는 존재하지 않습니다." + idx));
            return AcademyBoardDto.toAcademyBoardDto(entity);    
        } catch (EntityNotFoundException e) {
            log.error("Error occurred while getting a entity", e);
            throw e;
        }
    } 

//    public Map<String,Object> getDetailPage(int ab_idx, int m_idx){
//         //readcount 추가
//         academyBoardMapper.updateReadCount(ab_idx);
//
//        AcademyBoardEntity entity = academyBoardRepository.findById((Integer)ab_idx)
//            .orElseThrow(() -> new EntityNotFoundException("해당 idx는 존재하지 않습니다." + ab_idx));
//        AcademyBoardDto dto = AcademyBoardDto.toAcademyBoardDto(entity);
//
//         //필요한 변수들을 Map 에 담아서 보낸다
//         Map<String,Object> map=new HashMap<>();
//         map.put("ab_subject",dto.getAb_subject());
//         map.put("ab_content",dto.getAb_content());
//         map.put("ab_readcount",dto.getAb_readcount());
//         map.put("ab_photo",dto.getAb_photo());
//         map.put("ab_writeday",dto.getAb_writeday());
//         map.put("ab_like",dto.getAb_like());
//         map.put("ab_dislike",dto.getAb_dislike());
//         map.put("ai_idx",dto.getAi_idx());
//
//
//
//         map.put("cm_compname",academyBoardMapper.selectNickNameOfMidx(m_idx));
//         map.put("cm_filename",academyBoardMapper.selectPhotoOfMidx(m_idx));
//
//
//
//        return map;
//    }
public Map<String,Object> getDetailPage(int ab_idx){
        try{

            AcademyBoardEntity aboard = academyBoardRepository.findById(ab_idx)
                    .orElseThrow(() -> new EntityNotFoundException("해당 idx는 존재하지 않습니다." + ab_idx));
           aboard.setABreadcount(aboard.getABreadcount()+1);
           academyBoardRepository.save(aboard);

            // AIname 가져오기
        AcademyInfoEntity academyInfo = academyInfoRepository.findById(aboard.getAIidx()).orElse(null);
        MemberEntity memberInfo = memberRepository.findById(aboard.getMIdx()).orElse(null);

            Map<String, Object> aboarddetailInfo = new HashMap<>();
            aboarddetailInfo.put("aboard",AcademyBoardDto.toAcademyBoardDto(aboard));
            if (memberInfo != null) {
                aboarddetailInfo.put("mPhoto", memberInfo.getMPhoto());
                aboarddetailInfo.put("mNicname", memberInfo.getMNickname());
            }
            if(academyInfo !=null){
                aboarddetailInfo.put("ciNamge",academyInfo.getAIname());
            }
            return aboarddetailInfo;
        }catch (Exception e){
            log.error("Error finding one aboarddetail", e);
            throw e;
        }
        }




    // public void updateAcademyBoard(AcademyBoardDto dto,MultipartFile upload,int currentPage){

    //     String filename="";
    //     AcademyBoardEntity entity = academyBoardRepository.findById(dto.getAb_idx())
    //             .orElseThrow(() -> new EntityNotFoundException("해당 idx의 게시물이 존재하지 않습니다:" +dto.getAb_idx()));
    //     if(!upload.getOriginalFilename().equals("")) {
    //         filename= entity.getABphoto();
    //         storageService.deleteFile(bucketName,"devster/hireboard",filename);
    //         filename=storageService.uploadFile(bucketName, "devster/hireboard", upload);
    //     }
    //     try {
    //         entity.setABphoto(filename);
    //         academyBoardRepository.save(entity);
    //     } catch (Exception e) {
    //         log.error("Error occurred while inserting hireboard",e);
    //         throw e;
    //     }
    // }



    public void updateAcademyBoard(int ab_idx, AcademyBoardDto dto){
        try {
            Optional<AcademyBoardEntity> e = academyBoardRepository.findById(ab_idx);

            if(e.isPresent()) {
                AcademyBoardEntity existingEntity = e.get();
                existingEntity.setABsubject(dto.getAb_subject());
                existingEntity.setABcontent(dto.getAb_content());
//                existingEntity.setFBphoto(dto.getFb_photo());
                academyBoardRepository.save(existingEntity);
            }

        } catch (Exception e) {
            log.error("update AcademyBoard Error", e);
            throw e;
        }
    }

    public void updatePhoto(Integer ab_idx , MultipartFile upload ) {
        Optional<AcademyBoardEntity> entity = academyBoardRepository.findById(ab_idx);
        storageService.deleteFile(bucketName,"devster/aboard",entity.get().getABphoto());
        entity.get().setABphoto(storageService.uploadFile(bucketName,"devster/aboard",upload));
        academyBoardRepository.save(entity.get());

        log.info(ab_idx+" AcademyBoard 사진업데이트 완료");
    }




    public void deleteAcademyBoard(Integer idx){
        try {
            System.out.println(idx);
            academyBoardRepository.deleteById(idx);
        } catch (Exception e) {
            log.error("Error occurred while deleting a entity",e);
        }
    }



    


    private AcademylikeEntity findOrCreateABoardLike(int ABidx,int MIdx){
            return academylikeRepository.findByABidxAndMIdx(ABidx,MIdx)
                .orElse(new AcademylikeEntity(ABidx,MIdx));
    }    

    public void like(int ABidx, int MIdx){
        try {           
            AcademylikeEntity academylikeEntity = findOrCreateABoardLike(ABidx,MIdx);
            
            if (academylikeEntity.getLikestatus() == 1) {
                academylikeEntity.setLikestatus(0);
                academylikeRepository.save(academylikeEntity);
    
                AcademyBoardEntity academyBoardEntity = academyBoardRepository.findById(ABidx)
                    .orElseThrow(()-> new IllegalArgumentException("해당하는 리뷰 보드를 찾지 못했습니다(ab_like-1)"));
                academyBoardEntity.setABlike(academyBoardEntity.getABlike()-1);
                academyBoardRepository.save(academyBoardEntity);
    
            } else if (academylikeEntity.getLikestatus() == 2) {
                throw new IllegalArgumentException("이미 싫어요가 눌려 있습니다");
            } else {
                academylikeEntity.setLikestatus(1);
                academylikeRepository.save(academylikeEntity);
    
                // AcademyBoardEntity의 ab_like 필드 업데이트
                AcademyBoardEntity academyBoardEntity = academyBoardRepository.findById(ABidx)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 리뷰 보드를 찾지 못했습니다(ab_like+1): " + ABidx));
                academyBoardEntity.setABlike(academyBoardEntity.getABlike() + 1);
                academyBoardRepository.save(academyBoardEntity);
            }
        } catch (IllegalArgumentException e) {
            log.error("review like Error(Ill)", e);
        } catch (Exception e) {
         log.error("review like Error(Exce)", e);
        }
    }
     

    public void dislike(int ABidx, int MIdx) {
        try {
            AcademylikeEntity academylikeEntity = findOrCreateABoardLike(ABidx, MIdx);
            
            if (academylikeEntity.getLikestatus() == 1) {
                throw new IllegalArgumentException("이미 좋아요가 눌려 있습니다");
            } else if (academylikeEntity.getLikestatus() == 2) {
                //throw new IllegalArgumentException("이미 싫어요가 눌려 있습니다");
                academylikeEntity.setLikestatus(0);
                academylikeRepository.save(academylikeEntity);
    
                AcademyBoardEntity academyBoardEntity = academyBoardRepository.findById(ABidx)
                    .orElseThrow(()-> new IllegalArgumentException("해당하는 리뷰 보드를 찾지 못했습니다(ab_dislike-1)"));
                academyBoardEntity.setABdislike(academyBoardEntity.getABdislike()-1);
                academyBoardRepository.save(academyBoardEntity);
    
            } else {
                academylikeEntity.setLikestatus(2);
                academylikeRepository.save(academylikeEntity);
    
                // AcademyBoardEntity의 ab_like 필드 업데이트
                AcademyBoardEntity academyBoardEntity = academyBoardRepository.findById(ABidx)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 리뷰 보드를 찾지 못했습니다(rb_dislike+1): " + ABidx));
                academyBoardEntity.setABdislike(academyBoardEntity.getABdislike() + 1);
                academyBoardRepository.save(academyBoardEntity);
            }
        } catch (IllegalArgumentException e) {
            log.error("review like Error(Ill)", e);
        } catch (Exception e) {
         log.error("review like Error(Exce)", e);
        }
    }

    public boolean isAlreadyAddGoodRp(int ABidx, int MIdx){
        AcademylikeEntity academylikeEntity=findOrCreateABoardLike(ABidx, MIdx);
        return academylikeEntity.getLikestatus()==1;
    }
   
    public boolean isAlreadyAddBadRp(int ABidx, int MIdx){
        AcademylikeEntity academylikeEntity=findOrCreateABoardLike(ABidx, MIdx);
        return academylikeEntity.getLikestatus()==2;
    }


}






