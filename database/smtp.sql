PGDMP                        |            SMTP    16.2    16.2     �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            �           1262    16497    SMTP    DATABASE     y   CREATE DATABASE "SMTP" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_World.1252';
    DROP DATABASE "SMTP";
                postgres    false            �            1259    16499    Email    TABLE     �   CREATE TABLE public."Email" (
    "EmailID" integer NOT NULL,
    "From" text NOT NULL,
    "To" text NOT NULL,
    subject text,
    message text,
    attachment text
);
    DROP TABLE public."Email";
       public         heap    postgres    false            �            1259    16498    Email_EmailID_seq    SEQUENCE     �   CREATE SEQUENCE public."Email_EmailID_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public."Email_EmailID_seq";
       public          postgres    false    216            �           0    0    Email_EmailID_seq    SEQUENCE OWNED BY     M   ALTER SEQUENCE public."Email_EmailID_seq" OWNED BY public."Email"."EmailID";
          public          postgres    false    215            P           2604    16502    Email EmailID    DEFAULT     t   ALTER TABLE ONLY public."Email" ALTER COLUMN "EmailID" SET DEFAULT nextval('public."Email_EmailID_seq"'::regclass);
 @   ALTER TABLE public."Email" ALTER COLUMN "EmailID" DROP DEFAULT;
       public          postgres    false    216    215    216            �          0    16499    Email 
   TABLE DATA           X   COPY public."Email" ("EmailID", "From", "To", subject, message, attachment) FROM stdin;
    public          postgres    false    216   �
       �           0    0    Email_EmailID_seq    SEQUENCE SET     A   SELECT pg_catalog.setval('public."Email_EmailID_seq"', 6, true);
          public          postgres    false    215            R           2606    16506    Email Email_pkey 
   CONSTRAINT     Y   ALTER TABLE ONLY public."Email"
    ADD CONSTRAINT "Email_pkey" PRIMARY KEY ("EmailID");
 >   ALTER TABLE ONLY public."Email" DROP CONSTRAINT "Email_pkey";
       public            postgres    false    216            �   �   x�M�Mn� �5�b�X�M�]Dj���d3�SC�3�&�}��J^����8��������n�0Xǣ��6\�a���q���08O��q^n	�@�I�uF�l�/�N�N|%'�K3��O�[7��0FgS�a����`�)�H�.'�����r-e�B�
ۖ����?�;���Mm
�����p	l�J�����e{���=+�w�Dkk�����X����q�     