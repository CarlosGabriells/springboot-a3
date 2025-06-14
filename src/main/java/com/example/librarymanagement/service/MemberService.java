package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.MemberDto;
import com.example.librarymanagement.entity.Member;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.exception.BusinessException;
import com.example.librarymanagement.mapper.MemberMapper;
import com.example.librarymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada aos membros da biblioteca.
 * Gerencia operações CRUD, consultas específicas, e regras de negócio para entidade Member.
 * Inclui controle de status de associação, validação de unicidade de email e busca por critérios específicos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por padrão, todas as operações são somente leitura
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    /**
     * Busca todos os membros com paginação
     * @param pageable configurações de paginação
     * @return página com lista de membros
     */
    public Page<MemberDto> findAll(Pageable pageable) {
        log.debug("Finding all members with pagination: {}", pageable);
        return memberRepository.findAll(pageable)
                .map(memberMapper::toDto);
    }

    /**
     * Busca um membro pelo ID
     * @param id identificador do membro
     * @return dados do membro encontrado
     * @throws ResourceNotFoundException se o membro não for encontrado
     */
    public MemberDto findById(Long id) {
        log.debug("Finding member by id: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return memberMapper.toDto(member);
    }

    /**
     * Busca um membro pelo ID incluindo seus empréstimos
     * @param id identificador do membro
     * @return dados do membro encontrado com lista de empréstimos
     * @throws ResourceNotFoundException se o membro não for encontrado
     */
    public MemberDto findByIdWithLoans(Long id) {
        log.debug("Finding member by id with loans: {}", id);
        Member member = memberRepository.findByIdWithLoans(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return memberMapper.toDto(member);
    }

    /**
     * Busca um membro pelo endereço de email
     * @param email endereço de email do membro
     * @return dados do membro encontrado
     * @throws ResourceNotFoundException se o membro não for encontrado
     */
    public MemberDto findByEmail(String email) {
        log.debug("Finding member by email: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
        return memberMapper.toDto(member);
    }

    /**
     * Cria um novo membro no sistema
     * @param memberDto dados do membro a ser criado
     * @return dados do membro criado com ID gerado
     * @throws BusinessException se já existir um membro com o mesmo email
     */
    @Transactional
    public MemberDto create(MemberDto memberDto) {
        log.debug("Creating new member: {}", memberDto);
        
        if (memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new BusinessException("Member with email " + memberDto.getEmail() + " already exists");
        }

        Member member = memberMapper.toEntity(memberDto);
        Member savedMember = memberRepository.save(member);
        log.info("Created member with id: {}", savedMember.getId());
        return memberMapper.toDto(savedMember);
    }

    /**
     * Atualiza os dados de um membro existente
     * @param id identificador do membro a ser atualizado
     * @param memberDto novos dados do membro
     * @return dados do membro atualizado
     * @throws ResourceNotFoundException se o membro não for encontrado
     * @throws BusinessException se tentar alterar para um email que já existe
     */
    @Transactional
    public MemberDto update(Long id, MemberDto memberDto) {
        log.debug("Updating member with id: {}", id);
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        // Verifica se o email não está sendo usado por outro membro
        if (!existingMember.getEmail().equals(memberDto.getEmail()) && 
            memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new BusinessException("Member with email " + memberDto.getEmail() + " already exists");
        }

        // Atualiza os campos do membro existente
        existingMember.setFirstName(memberDto.getFirstName());
        existingMember.setLastName(memberDto.getLastName());
        existingMember.setEmail(memberDto.getEmail());
        existingMember.setPhone(memberDto.getPhone());
        existingMember.setAddress(memberDto.getAddress());
        existingMember.setMembershipDate(memberDto.getMembershipDate());
        existingMember.setStatus(memberDto.getStatus());

        Member updatedMember = memberRepository.save(existingMember);
        log.info("Updated member with id: {}", updatedMember.getId());
        return memberMapper.toDto(updatedMember);
    }

    /**
     * Remove um membro do sistema
     * @param id identificador do membro a ser removido
     * @throws ResourceNotFoundException se o membro não for encontrado
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting member with id: {}", id);
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
        log.info("Deleted member with id: {}", id);
    }

    /**
     * Busca membros por nome (primeiro ou último nome)
     * @param name nome ou parte do nome para busca
     * @param pageable configurações de paginação
     * @return página com lista de membros que correspondem ao critério
     */
    public Page<MemberDto> searchByName(String name, Pageable pageable) {
        log.debug("Searching members by name: {} with pagination: {}", name, pageable);
        return memberRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(memberMapper::toDto);
    }

    /**
     * Busca membros por status de associação
     * @param status status da associação (ACTIVE, SUSPENDED, CANCELLED)
     * @param pageable configurações de paginação
     * @return página com lista de membros com o status especificado
     */
    public Page<MemberDto> findByStatus(Member.MembershipStatus status, Pageable pageable) {
        log.debug("Finding members by status: {} with pagination: {}", status, pageable);
        return memberRepository.findByStatus(status, pageable)
                .map(memberMapper::toDto);
    }

    /**
     * Atualiza o status de associação de um membro
     * @param id identificador do membro
     * @param status novo status da associação
     * @return dados do membro com status atualizado
     * @throws ResourceNotFoundException se o membro não for encontrado
     */
    @Transactional
    public MemberDto updateStatus(Long id, Member.MembershipStatus status) {
        log.debug("Updating member status for id: {} to: {}", id, status);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        
        member.setStatus(status);
        Member updatedMember = memberRepository.save(member);
        log.info("Updated member status for id: {} to: {}", id, status);
        return memberMapper.toDto(updatedMember);
    }
}