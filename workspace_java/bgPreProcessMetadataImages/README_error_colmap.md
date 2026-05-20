

# patch_match_stereo

le calcul PatchMatch a réussi pour une vue, puis ça a cassé pendant “Writing geometric output”, et toutes les vues suivantes héritent d’un contexte CUDA déjà corrompu.

Le plantage n’a pas lieu pendant l’optimisation PatchMatch elle-même, mais pendant la phase de sortie géométrique / transfert / texture / buffers GPU associés à l’écriture.

Solution proposé: --PatchMatchStereo.geom_consistency 0